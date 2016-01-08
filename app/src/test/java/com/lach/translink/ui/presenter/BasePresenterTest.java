package com.lach.translink.ui.presenter;

import com.lach.common.async.AsyncResult;
import com.lach.common.async.Task;
import com.lach.common.async.TaskBuilder;
import com.lach.common.async.UnitTestingTaskBuilder;
import com.lach.translink.BaseTest;
import com.lach.translink.ui.view.TaskView;

import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public abstract class BasePresenterTest<P extends TaskPresenter<V>, V extends TaskView> extends BaseTest {

    public P presenter;
    public V view;

    @Before
    public void setup() {
        view = Mockito.mock(getViewClass());
        presenter = createPresenter();

        // Add task handling to prevent need for Android fragments.
        Mockito.when(view.createTask(Mockito.anyInt(), Mockito.any(Task.class))).thenAnswer(new Answer<TaskBuilder>() {
            @Override
            public TaskBuilder answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                return new UnitTestingTaskBuilder((int) arguments[0], (Task) arguments[1], new UnitTestingTaskBuilder.PostExecuteListener() {
                    @Override
                    public void onTaskExecuted(int taskId, AsyncResult result) {
                        int errorId = result.getErrorId();

                        if (!result.hasError()) {
                            presenter.onTaskFinished(taskId, result);
                        } else {
                            presenter.onTaskError(taskId, errorId);
                        }
                    }
                });
            }
        });
        postSetup();

        // There is no view state initially.
        presenter.onCreate(view, null);
        postCreate();
    }

    /**
     * Calls the pseudo life-cycle on the presenter.
     */
    public void recreate() {
        ViewState viewState = new InMemoryViewState();
        presenter.saveState(viewState);
        presenter.onDestroy();
        presenter.onCreate(view, viewState);
        postCreate();
    }

    public void postCreate() {
        // Do nothing.
    }

    public abstract P createPresenter();

    public abstract Class<V> getViewClass();

    public abstract void postSetup();

}
