/*
 * Copyright (c) 2007-2009 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */

package cascading.flow.hadoop;

import cascading.flow.FlowException;
import cascading.stats.StepStats;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TaskCompletionEvent;
import org.apache.hadoop.mapred.TaskReport;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

/** Class HadoopStepStats ... */
public abstract class HadoopStepStats extends StepStats
  {
  /** Field LOG */
  private static final Logger LOG = Logger.getLogger( HadoopStepStats.class );

  /** Field numMapTasks */
  int numMapTasks;
  /** Field numReducerTasks */
  int numReducerTasks;
  /** Field taskStats */
  ArrayList<HadoopTaskStats> taskStats;

  /** Class HadoopTaskStats ... */
  public static class HadoopTaskStats
    {
    public enum TaskType
      {
        SETUP, MAPPER, REDUCER, CLEANUP
      }

    /** Field taskType */
    TaskType taskType;
    /** Field id */
    String id;
    /** Field startTime */
    long startTime;
    /** Field finishTime */
    long finishTime;
    /** Field status */
    String status;
    /** Field state */
    String state;

    public HadoopTaskStats( TaskType taskType, TaskReport taskReport )
      {
      fill( taskType, taskReport );
      }

    public HadoopTaskStats( TaskCompletionEvent taskCompletionEvent )
      {
      fill( taskCompletionEvent );
      }

    public String getId()
      {
      return id;
      }

    public void fill( TaskCompletionEvent taskCompletionEvent )
      {
      taskType = taskCompletionEvent.getTaskAttemptId().getTaskID().isMap() ? TaskType.MAPPER : TaskType.REDUCER;
      status = taskCompletionEvent.getTaskStatus().toString();
      }

    public void fill( TaskType taskType, TaskReport taskReport )
      {
      this.taskType = taskType;
      this.id = taskReport.getTaskID().toString();
      this.startTime = taskReport.getStartTime();
      this.finishTime = taskReport.getFinishTime();
      this.state = taskReport.getState();
      this.status = TaskCompletionEvent.Status.SUCCEEDED.toString();
      }
    }

  public ArrayList<HadoopTaskStats> getTaskStats()
    {
    if( taskStats == null )
      taskStats = new ArrayList<HadoopTaskStats>();

    return taskStats;
    }

  private void addTaskStats( HadoopTaskStats.TaskType taskType, TaskReport[] taskReports, boolean skipLast )
    {
    for( int i = 0; i < taskReports.length - ( skipLast ? 1 : 0 ); i++ )
      getTaskStats().add( new HadoopTaskStats( taskType, taskReports[ i ] ) );
    }

  private void addTaskStats( TaskCompletionEvent[] events )
    {
    for( TaskCompletionEvent event : events )
      {
      if( event.getTaskStatus() != TaskCompletionEvent.Status.SUCCEEDED )
        getTaskStats().add( new HadoopTaskStats( event ) );
      }
    }

  public int getNumMapTasks()
    {
    return numMapTasks;
    }

  public void setNumMapTasks( int numMapTasks )
    {
    this.numMapTasks = numMapTasks;
    }

  public int getNumReducerTasks()
    {
    return numReducerTasks;
    }

  public void setNumReducerTasks( int numReducerTasks )
    {
    this.numReducerTasks = numReducerTasks;
    }

  protected abstract JobClient getJobClient();

  protected abstract RunningJob getRunningJob();

  @Override
  public long getCounter( Enum counter )
    {
    try
      {
      return getRunningJob().getCounters().getCounter( counter );
      }
    catch( IOException e )
      {
      throw new FlowException( "unable to get counter values" );
      }
    }

  public void captureJobStats()
    {
    RunningJob runningJob = getRunningJob();

    JobConf ranJob = new JobConf( runningJob.getJobFile() );

    setNumMapTasks( ranJob.getNumMapTasks() );
    setNumReducerTasks( ranJob.getNumReduceTasks() );
    }

  @Override
  public void captureDetail()
    {
    getTaskStats().clear();

    JobClient jobClient = getJobClient();

    try
      {
      addTaskStats( HadoopTaskStats.TaskType.SETUP, jobClient.getSetupTaskReports( getRunningJob().getID() ), true );
      addTaskStats( HadoopTaskStats.TaskType.MAPPER, jobClient.getMapTaskReports( getRunningJob().getID() ), false );
      addTaskStats( HadoopTaskStats.TaskType.REDUCER, jobClient.getReduceTaskReports( getRunningJob().getID() ), false );
      addTaskStats( HadoopTaskStats.TaskType.CLEANUP, jobClient.getCleanupTaskReports( getRunningJob().getID() ), true );

      int count = 0;

      while( true )
        {
        TaskCompletionEvent[] events = getRunningJob().getTaskCompletionEvents( count );

        if( events.length == 0 )
          break;

        addTaskStats( events );
        count += 10;
        }
      }
    catch( IOException exception )
      {
      LOG.warn( "unable to get task stats", exception );
      }
    }

  }