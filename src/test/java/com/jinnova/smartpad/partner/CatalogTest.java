package com.jinnova.smartpad.partner;

import java.sql.SQLException;

import com.jinnova.smartpad.IPagingList;

import junit.framework.TestCase;

public class CatalogTest extends TestCase {

	public void testCreateSub() throws SQLException {
        SmartpadCommon.initialize("localhost", null, "smartpad", "root", "", "../app-server/imaging/in-queue", "../app-server/imaging/root");
        IUser u = SmartpadCommon.partnerManager.login("lotte", "lotte");
        IPagingList<ICatalog, ICatalogSort> paging = u.getBranch().getRootCatalog().getSubCatalogPagingList();
        ICatalog[] cats = paging.loadPage(u, 1).getPageEntries();
        
        paging = cats[0].getSubCatalogPagingList();
        ICatalog newCat = paging.newEntryInstance(u);
        newCat.setName("test");
        paging.put(u, newCat);
	}
	
	public void testBranchRootCat() throws SQLException {
		SmartpadCommon.initialize("localhost", null, "smartpad", "root", "", "../app-server/imaging/in-queue", "../app-server/imaging/root");
        IUser u = SmartpadCommon.partnerManager.login("lotte", "lotte");
        ICatalog rootCat = u.getBranch().getRootCatalog();
        assertNotNull(rootCat);
        assertNotNull(rootCat.getName());
        String s = u.getBranch().getRootCatalog().getSystemCatalog().getName();
        System.out.println(s);
        assertNotNull(s);
	}
}
