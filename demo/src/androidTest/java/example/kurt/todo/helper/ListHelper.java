/*
 * Copyright (c) 2012 Kurt Mbanje
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   ckurtm at gmail dot com
 *   https://github.com/ckurtm/DroidProvider
 */

package example.kurt.todo.helper;

import android.app.Instrumentation;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by kurt on 2014/07/21.
 */
public class ListHelper {

    public static View getViewAtIndex(final ListView listView, final int indexInList, Instrumentation instrumentation) {
        if (listView != null) {
            if (indexInList <= listView.getAdapter().getCount()) {
                scrollListTo(listView, indexInList, instrumentation);
                int indexToUse = indexInList - listView.getFirstVisiblePosition();
                return listView.getChildAt(indexToUse);
            }
        }
        return null;
    }

    public static <T extends AbsListView> void scrollListTo(final T listView, final int index, Instrumentation instrumentation) {
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(index);
            }
        });
        instrumentation.waitForIdleSync();
    }
}
