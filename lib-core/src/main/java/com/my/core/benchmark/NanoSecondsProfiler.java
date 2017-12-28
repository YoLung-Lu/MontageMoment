// Copyright (c) 2017-present boyw165
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//    The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.my.core.benchmark;

import com.my.core.protocol.IProfiler;
import com.my.core.protocol.ISystemClock;

import java.util.Stack;

public class NanoSecondsProfiler implements IProfiler,
                                            ISystemClock {

    private Stack<Long> timeStamps = new Stack<>();

    @Override
    public void startProfiling() {
        timeStamps.push(getCurrentTimeNanos());
    }

    @Override
    public float stopProfilingAndCalculateInterval() {
        if (timeStamps.isEmpty()) {
            return 0f;
        } else {
            return (float) (getCurrentTimeNanos() - timeStamps.pop()) / 1000000f;
        }
    }

    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public long getCurrentTimeNanos() {
        return System.nanoTime();
    }
}
