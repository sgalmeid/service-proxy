package com.predic8.membrane.core.interceptor.websocket;

import com.predic8.membrane.core.transport.ws.WebSocketFrame;
import com.predic8.membrane.core.transport.ws.WebSocketFrameAssembler;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WebSocketFrameTest {
    static int numberOfFrames = 100000;

    @Test
    public void testDynamicBuffer() throws Exception {


        InputStream is = new InputStream() {
            int sentFrames = 0;
            boolean first = true;

            byte[] frame = new WebSocketFrame(false, false, false, false, 2, true, new byte[4], new byte[8192 - 8 + 1]).toBytes();
            int counter = 0;

            @Override
            public int read() throws IOException {
                int my = counter++ % frame.length;
                if(counter == frame.length)
                    counter = 0;
                if (!first && sentFrames % numberOfFrames == 0)
                    return -1;
                if (counter == 0) {
                    first = false;
                    sentFrames++;
                }
                return frame[my];

            }
        };

        final int[] counter = {0};
        WebSocketFrameAssembler wsfa = new WebSocketFrameAssembler(is, null);
        wsfa.readFrames(webSocketFrame -> {
            try {
                counter[0]++;
                assertEquals(8193, webSocketFrame.toBytes().length);
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        });
        assertEquals(numberOfFrames, counter[0]);

    }
}
