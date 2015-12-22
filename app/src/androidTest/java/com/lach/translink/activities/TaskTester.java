package com.lach.translink.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.CountingIdlingResource;
import android.support.test.rule.ActivityTestRule;

import com.lach.common.async.AsyncTaskRunner;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;

public abstract class TaskTester {
    private final AsyncTaskRunner taskRunner;
    private final ActivityTestRule activityTestRule;

    private CountDownLatch countDownLatch;
    private CountingIdlingResource countingIdlingResource;

    public TaskTester(AsyncTaskRunner taskRunner, ActivityTestRule activityTestRule) {
        this.taskRunner = taskRunner;
        this.activityTestRule = activityTestRule;
    }

    /**
     * Setup the plumbing for the task tester which handles testing the ui before the task executes.
     */
    public void setup() {
        countDownLatch = new CountDownLatch(1);
        countingIdlingResource = new CountingIdlingResource("UserTaskTester");

        // Prevents the postTask from firing until the User Task has finished.
        countingIdlingResource.increment();

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                // Execute an anonymous thread to prevent Espresso waiting.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Wait until the preTask method has finished.
                            countDownLatch.await();

                            // Once the latch is released, get back on the ui thread and execute the task.
                            activityTestRule.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AsyncTask task = (AsyncTask) invocation.getArguments()[0];
                                    Object[] params = (Object[]) invocation.getArguments()[1];
                                    task.execute(params);

                                    // Allow the post task to fire.
                                    countingIdlingResource.decrement();

                                }
                            });
                        } catch (Throwable ignored) {

                        }
                    }
                }).start();
                return null;
            }
        }).when(taskRunner).execute(Mockito.any(AsyncTask.class), Mockito.anyVararg());
    }

    /**
     * Fires the pre-task, then unblocks the current thread and then fires the post-task.
     */
    public void execute(Activity activity) {
        preTask(activity);

        // Ensure that the post task tests idles until the User Task has finished.
        Espresso.registerIdlingResources(countingIdlingResource);

        countDownLatch.countDown();
        postTask(activity);

        // Don't idle anymore.
        Espresso.unregisterIdlingResources(countingIdlingResource);
    }

    /**
     * Provides the ability to fire any logic before the user task thread is unlocked.
     */
    public abstract void preTask(Activity activity);

    /**
     * Provides the ability to fire any logic after the user task thread is run.
     */
    public abstract void postTask(Activity activity);

}