package fitnesse.slimTables;

import fitnesse.responders.run.slimResponder.SlimTestContext;

import java.util.List;

public class SubsetQueryTable extends QueryTable {

    public SubsetQueryTable(Table table, String id, SlimTestContext testContext) {
        super(table, id, testContext);
    }

    @Override
    protected void scanRowsForMatches(List<Object> queryResultList) throws Exception {
        queryResults = new QueryResults(queryResultList);
        int rows = table.getRowCount();
        for (int tableRow = 2; tableRow < rows; tableRow++)
            scanRowForMatch(tableRow);
    }

}
