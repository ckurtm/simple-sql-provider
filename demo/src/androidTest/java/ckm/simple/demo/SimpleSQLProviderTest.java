package ckm.simple.demo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;



import java.util.List;

import ckm.simple.demo.models.Parent;
import ckm.simple.demo.models.ParentTable;
import ckm.simple.demo.models.Person;
import ckm.simple.demo.models.PersonTable;


/**
 * Created by simple on 2015/08/31.
 */
public class SimpleSQLProviderTest extends ProviderTestCase2<SimpleSQLProvider> {

    String TAG = SimpleSQLProviderTest.class.getSimpleName();

    Parent[] parents = new Parent[]{
            new Parent(1,"Minato","Namikaze"),
            new Parent(2,"Fugaku","Uchiha"),
    };

    Person[] persons = new Person[]{
            new Person(10,"Naruto","Uzumaki",1),
            new Person(20,"Sasuke","Uchiha",2),
            new Person(21,"Itachi","Uchiha",2)
    };

    private MockContentResolver resolver;
    private SQLiteDatabase database;


    public SimpleSQLProviderTest() {
        super(SimpleSQLProvider.class, SimpleSQLProvider.AUTHORITY);
    }


    protected void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
        database = getProvider().getDatabaseHelper().getWritableDatabase();
    }


    public void testParentInsert() throws Exception {
        for (Parent parent : parents) {
            Uri uri = resolver.insert(ParentTable.CONTENT_URI, ParentTable.getContentValues(parent));
            Log.d(TAG,"[]+ " + uri);
            assertNotNull(uri);
        }
        deleteParentData();
    }

    public void testPersonInsert() throws Exception {
        for (Person person : persons) {
            Uri uri = resolver.insert(PersonTable.CONTENT_URI, PersonTable.getContentValues(person));
            Log.d(TAG,"[]+ " + uri);
            assertNotNull(uri);
        }
        deletePersonData();
    }


    public void testParentDelete() throws Exception {
        insertParentData();
        int result = resolver.delete(ParentTable.CONTENT_URI,null,null);
        assertEquals(result, parents.length);
    }

    public void testPersonDelete() throws Exception {
        insertPersonData();
        int result = resolver.delete(PersonTable.CONTENT_URI,null,null);
        assertEquals(result, persons.length);
    }

    public void testParentQuery() throws Exception {
        insertParentData();
        Cursor cursor = resolver.query(ParentTable.CONTENT_URI, null, null, null, ParentTable.FIELD_PID + " ASC");
        List<Parent> entries = ParentTable.getValues(cursor, true);
        assertEquals(entries.size(), parents.length);
        deleteParentData();
    }

    public void testPersonQuery() throws Exception {
        insertPersonData();
        Cursor cursor = resolver.query(PersonTable.CONTENT_URI, null, null, null, PersonTable.FIELD_ID + " ASC");
        List<Person> entries = PersonTable.getValues(cursor,true);
        assertEquals(entries.size(), persons.length);
        deletePersonData();
    }


    public void testChildredQuery() throws Exception {
        insertPersonData();
        int parentId = 2;
        Cursor cursor = resolver.query(PersonTable.CONTENT_URI, null, PersonTable.FIELD_PARENTID + "=?", new String[]{String.valueOf(parentId)}, null);
        List<Person> entries = PersonTable.getValues(cursor, true);
        assertEquals(entries.size(), 2);
        deletePersonData();
    }


    public void testParentUpdate() throws Exception {
        Parent oldParent = parents[0];
        int oldId = oldParent.parentId;
        database.insertOrThrow(ParentTable.TABLE_NAME, null, ParentTable.getContentValues(oldParent));
        oldParent.parentId = -1;
        int updated = resolver.update(ParentTable.CONTENT_URI, ParentTable.getContentValues(oldParent), ParentTable.FIELD_PID + "=?", new String[]{String.valueOf(oldId)});
        assertEquals(1, updated);
        Cursor cursor = resolver.query(ParentTable.CONTENT_URI, null, null, null, ParentTable.FIELD_PID + " ASC");
        assertNotNull(cursor);
        cursor.moveToNext();
        Parent newParent = ParentTable.getValue(cursor,true);
        assertEquals(newParent.parentId, -1);
        deleteParentData();
    }


    public void testPersonUpdate() throws Exception {
        Person oldPerson = persons[0];
        int oldId = oldPerson.id;
        database.insertOrThrow(PersonTable.TABLE_NAME, null, PersonTable.getContentValues(oldPerson));
        oldPerson.id = -1;
        int updated = resolver.update(PersonTable.CONTENT_URI, PersonTable.getContentValues(oldPerson), PersonTable.FIELD_ID + "=?", new String[]{String.valueOf(oldId)});
        assertEquals(1, updated);
        Cursor cursor = resolver.query(PersonTable.CONTENT_URI, null, null, null, PersonTable.FIELD_ID + " ASC");
        assertNotNull(cursor);
        cursor.moveToNext();
        Person newPerson = PersonTable.getValue(cursor,true);
        assertEquals(newPerson.id, -1);
        deletePersonData();
    }

    private void insertParentData() {
        for (Parent parent : parents) {
            database.insertOrThrow(ParentTable.TABLE_NAME, null, ParentTable.getContentValues(parent));
        }
    }

    private void insertPersonData() {
        for (Person person : persons) {
            database.insertOrThrow(PersonTable.TABLE_NAME, null, PersonTable.getContentValues(person));
        }
    }


    private void deleteParentData() {
        database.delete(ParentTable.TABLE_NAME, null, null);
    }


    private void deletePersonData() {
        database.delete(PersonTable.TABLE_NAME, null, null);
    }
}
