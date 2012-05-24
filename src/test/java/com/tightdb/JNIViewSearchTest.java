package com.tightdb;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JNIViewSearchTest {

	@Test
	public void shouldSearchByColumnValue() {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeString, "name");
		table.updateFromSpec(tableSpec);

		table.insertString(0, 0, "Foo");
		table.insertDone();

		table.insertString(0, 1, "Bar");
		table.insertDone();

		TableQuery query = new TableQuery();
		TableViewBase view = query.findAll(table, 0, table.size(), Integer.MAX_VALUE);
		assertEquals(2, view.size());

		view.findAllString(0, "Foo");
	}

}