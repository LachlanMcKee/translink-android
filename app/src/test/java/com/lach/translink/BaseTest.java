package com.lach.translink;

import com.lach.common.log.Instrumentation;
import com.lach.common.log.Log;
import com.lach.translink.ui.impl.resolve.ResolveLocationEvents;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import de.greenrobot.event.EventBus;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {Instrumentation.class, Log.class, EventBus.class})
public abstract class BaseTest {

    private EventBusAnswer mEventBusAnswer;

    @Before
    public void beforeTests() {
        // Ensure that the instrumentation doesn't trigger.
        PowerMockito.mockStatic(Instrumentation.class);

        // Mock the log class so it redirects to the standard system console for testing.
        PowerMockito.mockStatic(Log.class);

        mockLogMethod("debug");
        Log.debug(Mockito.anyString(), Mockito.anyString());

        mockLogMethod("warning");
        Log.warn(Mockito.anyString(), Mockito.anyString());

        mockLogMethod("warning");
        Log.warn(Mockito.anyString(), Mockito.anyString(), Mockito.any(Exception.class));

        mockLogMethod("error");
        Log.error(Mockito.anyString(), Mockito.anyString());

        mockLogMethod("error");
        Log.error(Mockito.anyString(), Mockito.anyString(), Mockito.any(Exception.class));

        // Handle any event bus usage.
        mEventBusAnswer = null;

        EventBus mockedEventBus = Mockito.mock(EventBus.class);
        PowerMockito.mockStatic(EventBus.class);
        PowerMockito.when(EventBus.getDefault()).thenReturn(mockedEventBus);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (mEventBusAnswer != null) {
                    Object event = invocation.getArguments()[0];
                    mEventBusAnswer.test(event);
                }
                return null;
            }
        }).when(mockedEventBus).post(Mockito.any());
    }

    public void setMockedEventBusAnswer(EventBusAnswer eventBusAnswer) {
        mEventBusAnswer = eventBusAnswer;
    }

    private void mockLogMethod(final String messageType) {
        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();

                System.out.println("Found " + messageType + " message: " + arguments[0] + ": " + arguments[1]);

                return null;
            }
        }).when(Log.class);
    }

    /**
     * Reads a specified file from the resources folder into a string.
     *
     * @param fileName the name of the file being read.
     * @return the contents of the file.
     * @throws IOException thrown if the input file cannot be read.
     */
    public String readFileFromResources(String fileName) throws IOException {
        return IOUtils.toString(ClassLoader.getSystemResourceAsStream(fileName), "UTF-8");
    }

    public interface EventBusAnswer {
        void test(Object event);
    }

}
