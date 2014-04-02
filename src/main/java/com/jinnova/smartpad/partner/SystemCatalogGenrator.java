package com.jinnova.smartpad.partner;

import java.sql.SQLException;

class SystemCatalogGenrator {
	
	static void generate() throws SQLException {
		
		//system catalog
		PartnerManager pm = PartnerManager.instance;
		ICatalog sysCatFoods = pm.getSystemRootCatalog().getSubCatalogPagingList().newMemberInstance(pm.getSystemUser());
		sysCatFoods.getName().setName("Foods");
		sysCatFoods.getCatalogSpec().setSpecId("foods"); //table name
		ICatalogField field = sysCatFoods.getCatalogSpec().createField();
		field.setId("name"); //column name
		field.setFieldType(ICatalogFieldType.Text_Name);
		field.setName("Name");
		pm.getSystemRootCatalog().getSubCatalogPagingList().put(pm.getSystemUser(), sysCatFoods);
	}

}
