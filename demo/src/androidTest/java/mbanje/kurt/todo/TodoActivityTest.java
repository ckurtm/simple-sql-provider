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

package mbanje.kurt.todo;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.robotium.solo.Solo;

import mbanje.kurt.todo.helper.ListHelper;
import mbanje.kurt.todo.provider.TodoHelper;
import mbanje.kurt.todo.ui.TodoActivity;
import mbanje.kurt.todo.ui.TodoAdapter;
import mbanje.kurt.todo.widget.ProgressView;

/**
 * Created by kurt on 2014/07/19.
 */
public class TodoActivityTest extends ActivityInstrumentationTestCase2<TodoActivity> {
    private Solo solo;

    private Todo[] items = new Todo[]{
            new Todo("One", "description for task one", false),
            new Todo("Two", "description for task two", false),
            new Todo("Three", "description for task three", false),
            new Todo("Four", "description for task four", false),
            new Todo("Five", "description for task five", false),
            new Todo("Six", "description for task six", false)
    };

    /**
     * Creates an {@link ActivityInstrumentationTestCase2} for the {@link TodoActivity} activity.
     */
    public TodoActivityTest() {
        super(TodoActivity.class);
    }

    /**
     * Verifies that the activity under test can be launched.
     */
    public void testActivityTestCaseSetUpProperly() {
        assertNotNull("activity should be launched successfully", getActivity());
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());

    }

    public void testAddItems() throws Exception {
        TodoHelper.deleteAll(getActivity().getContentResolver());
        ListView list = (ListView) solo.getView(R.id.todo_list_items);
        TodoAdapter adapter = (TodoAdapter) list.getAdapter();
        assertNotNull(list);
        assertNotNull(adapter);
        for (Todo item : items) {
            addItem(item);
        }
        assertEquals(adapter.getCount(), items.length - 1);
    }

    public void testRemoveItems() throws Exception {
        TodoHelper.deleteAll(getActivity().getContentResolver());
        for (Todo item : items) {
            TodoHelper.createTodo(getActivity().getContentResolver(), item);
        }
        solo.clickLongInList(2);
        View actionitem = solo.getView(R.id.action_delete);
        assertNotNull(actionitem);
        solo.clickInList(0);
        solo.clickOnView(actionitem);
        solo.waitForLogMessage("adapter updated");
    }


    public void testMarkComplete() throws Exception {
        TodoHelper.deleteAll(getActivity().getContentResolver());
        final ListView list = (ListView) solo.getView(R.id.todo_list_items);
        final TodoAdapter adapter = (TodoAdapter) list.getAdapter();
        assertNotNull(list);
        assertNotNull(adapter);
        for (Todo item : items) {
            TodoHelper.createTodo(getActivity().getContentResolver(), item);
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
        solo.waitForLogMessage("added new task");
        ListHelper.scrollListTo(list, 5, getInstrumentation());
        View last = ListHelper.getViewAtIndex(list, 4, getInstrumentation());
        assertNotNull(last);
        CheckBox box = (CheckBox) last.findViewById(R.id.item_completed);
        assertNotNull(box);
        solo.clickOnView(box);
        solo.waitForLogMessage("view updated");
        ProgressView progressBar = solo.getView(ProgressView.class, 0);
        assertNotNull(progressBar);
        assertEquals(17, progressBar.getPercent());
        ListHelper.scrollListTo(list, 1, getInstrumentation());
        last = ListHelper.getViewAtIndex(list, 0, getInstrumentation());
        assertNotNull(last);
        box = (CheckBox) last.findViewById(R.id.item_completed);
        assertNotNull(box);
        solo.clickOnView(box);
        solo.waitForLogMessage("view updated");
        solo.waitForLogMessage("just waiting ...", 3000);
        assertEquals(33, progressBar.getPercent());
    }

    private void addItem(Todo item) {
        String label = item.label;
        String description = item.description;
        View view = solo.getView(R.id.todo_list_add);
        solo.clickOnView(view);
        solo.waitForDialogToOpen();
        EditText vLabel = (EditText) solo.getView(R.id.todo_dialog_label);
        solo.clearEditText(vLabel);
        solo.enterText(vLabel, label);
        EditText vDesc = (EditText) solo.getView(R.id.todo_dialog_description);
        solo.clearEditText(vDesc);
        solo.enterText(vDesc, description);
        solo.clickOnButton("Save");
        solo.waitForLogMessage("added new task");
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
