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
	
	public void testManagedBranchName() throws SQLException {
		SmartpadCommon.initialize("localhost", null, "smartpad", "root", "", "../app-server/imaging/in-queue", "../app-server/imaging/root");
		 IUser u = SmartpadCommon.partnerManager.login("lotte", "lotte");
        ICatalogItem item = u.getBranch().getRootCatalog().getCatalogItemPagingList().newEntryInstance(u);
        item.setField(ICatalogField.F_NAME, "test");
        u.getBranch().getRootCatalog().getCatalogItemPagingList().put(u, item);
	}
	
	public void testMissingBranchName() throws SQLException {
		SmartpadCommon.initialize("localhost", null, "smartpad", "root", "", "../app-server/imaging/in-queue", "../app-server/imaging/root");
		IUser u = SmartpadCommon.partnerManager.createPrimaryUser("test", "test");
		u.getBranch().setSystemCatalogId("z");
		u.updateBranch();
		//IUser u = SmartpadCommon.partnerManager.login("test", "test");
        //ICatalog syscat = SmartpadCommon.partnerManager.getSystemCatalog("z");
        ICatalogItem item = u.getBranch().getRootCatalog().getCatalogItemPagingList().newEntryInstance(u);
        item.setField(ICatalogField.F_NAME, "test");
        try {
        	u.getBranch().getRootCatalog().getCatalogItemPagingList().put(u, item);
        	assert(false);
        } catch (RuntimeException e) {
        	
        }
	}
	
	public void testAddSyscatItem() throws SQLException {
		SmartpadCommon.initialize("localhost", null, "smartpad", "root", "", "../app-server/imaging/in-queue", "../app-server/imaging/root");
		IUser u = SmartpadCommon.partnerManager.login("test", "test");
		u.getBranch().setSystemCatalogId("z");
		u.updateBranch();
		//IUser u = SmartpadCommon.partnerManager.login("test", "test");
        //ICatalog syscat = SmartpadCommon.partnerManager.getSystemCatalog("z");
        ICatalogItem item = u.getBranch().getRootCatalog().getCatalogItemPagingList().newEntryInstance(u);
        item.setField(ICatalogField.F_NAME, "test2");
        item.setBranchName("branch 2");
        u.getBranch().getRootCatalog().getCatalogItemPagingList().put(u, item);
	}
}
