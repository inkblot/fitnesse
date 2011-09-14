package fitnesse.slimTables;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class SlimTableFactoryTest {
    private SlimTableFactory slimTableFactory;
    private Table table;
    private Map<String, Class<? extends SlimTable>> map;

    @Before
    public void setUp() {
        slimTableFactory = new SlimTableFactory();
        table = mock(Table.class);
        map = new HashMap<String, Class<? extends SlimTable>>();
        map.put("dt:", DecisionTable.class);
        map.put("dT:", DecisionTable.class);
        map.put("decision:", DecisionTable.class);
        map.put("ordered query:", OrderedQueryTable.class);
        map.put("subset query:", SubsetQueryTable.class);
        map.put("query:", QueryTable.class);
        map.put("table:", TableTable.class);
        map.put("script", ScriptTable.class);
        map.put("scenario", ScenarioTable.class);
        map.put("import", ImportTable.class);
        map.put("something", DecisionTable.class);
        map.put("library", LibraryTable.class);
    }

    @Test
    public void shouldCreateCorrectSlimTableForTablesType() {
        Set<Entry<String,Class<? extends SlimTable>>> entrySet = map.entrySet();

        for (Entry<String,Class<? extends SlimTable>> entry : entrySet) {
            assertThatTableTypeCreateSlimTableType(entry.getKey(), entry.getValue());

        }
    }

    private void assertThatTableTypeCreateSlimTableType(String tableType, Class expectedClass) {
        when(table.getCellContents(0, 0)).thenReturn(tableType);
        SlimTable slimTable = slimTableFactory.makeSlimTable(table, "0", new MockSlimTestContext());
        String message = "should have created a " + expectedClass + " for tabletype: " + tableType
                + " but was " + slimTable.getClass();
        assertThat(message, slimTable, instanceOf(expectedClass));
    }
}
