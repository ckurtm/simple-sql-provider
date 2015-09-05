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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.test.ProviderTestCase2;

import java.io.IOException;

import mbanje.kurt.todo.provider.TodoHelper;

/**
 * Created by kurt on 2014/07/19.
 */
public class TodoHelperTest extends ProviderTestCase2<TodoProvider> {
    String TAG = TodoHelperTest.class.getSimpleName();
    private ContentResolver resolver;

    public TodoHelperTest() {
        super(TodoProvider.class, "mbanje.kurt.todo.debug");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resolver = getContext().getContentResolver();
    }

    public void testInsertDelete() throws IOException {
        //INSERT
        Todo item = new Todo("one", "the description", false);
        Uri uri = TodoHelper.createTodo(resolver, item);
        assertNotNull(uri);
        //DELETE
        long id = ContentUris.parseId(uri);
        item.id = id;
        int result = TodoHelper.deleteTodo(resolver, item);
        assertEquals(1, result);
    }


    public void testInsertQuery() throws IOException {
        //INSERT
        Todo item = new Todo("one2", "the description", false);
        Uri uri = TodoHelper.createTodo(resolver, item);
        assertNotNull(uri);
        long id = ContentUris.parseId(uri);

        //QUERY by ID
        Todo upItem = TodoHelper.getTodo(resolver, id);
        assertNotNull(upItem);
        assertEquals("one2", upItem.label);

        //QUERY by Key
        upItem = TodoHelper.getTodo(resolver, "one2");
        assertNotNull(upItem);
        assertEquals("one2", upItem.label);
    }

    public void testInsertUpdateQueryDelete() throws Exception {
        //INSERT
        Todo item = new Todo("one", "the description", false);
        Uri uri = TodoHelper.createTodo(resolver, item);
        assertNotNull(uri);
        long id = ContentUris.parseId(uri);
        item.id = id;

        //UPDATE
        item.label = "kurt";
        int updates = TodoHelper.updateTodo(resolver, item);
        assertEquals(1, updates);

        //QUERY
        Todo upItem = TodoHelper.getTodo(resolver, id);
        assertNotNull(upItem);
        assertEquals("kurt", upItem.label);

        //DELETE
        int result = TodoHelper.deleteTodo(resolver, upItem);
        assertEquals(1, result);
    }

//    public void testCanGetProviderAndDB() throws Exception {
//        ContentProviderClient client = resolver.acquireContentProviderClient(ProviderContract.CONTENT_AUTHORITY);
//        assertNotNull(client);
//        ContentProvider contentProvider = client.getLocalContentProvider();
//        assertNotNull(contentProvider);
//        TodoProvider provider = (TodoProvider) contentProvider;
//        assertNotNull(provider);
//        TodoSqlHelper dataStore = (TodoSqlHelper) provider.getMyDB();
//        assertNotNull(dataStore);
//    }


//    public void testPerformance() throws Exception {
//        resolver.delete(TodoTable.CONTENT_URI,null,null);
//        int sample = 10;
//        long[] values = new long[sample];
//        for (int index = 0; index < sample ; index++) {
//            long start = SystemClock.uptimeMillis();
//            runInsertionTest(100);
//            values[index] = SystemClock.uptimeMillis() - start;
//            Log.d(TAG,"...["+index+"] " + values[index] + " ...");
//        }
//
//        long total = 0;
//        for (int i = 0; i < sample; i++) {
//            total += values[i];
//        }
//
//        Log.d(TAG,"[sample:"+sample+"] [average:"+ ((float)total/(float)sample) + "]");
//    }



    private void runInsertionTest(int count) throws Exception {
        Todo item = new Todo("label","thisis a description",false);
        for (int i = 0; i < count; i++) {
            ContentValues values = TodoTable.getContentValues(item,false);
            resolver.insert(TodoTable.CONTENT_URI,values);
        }
    }
}
