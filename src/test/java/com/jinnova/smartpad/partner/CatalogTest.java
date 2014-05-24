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

	public void testSystemCatListing() throws SQLException {
        SmartpadCommon.initialize("localhost", null, "smartpad", "root", "", "../app-server/imaging/in-queue", "../app-server/imaging/root");
        IUser u = SmartpadCommon.partnerManager.login("ngoc", "ngoc");
        ICatalogItem[] items = PartnerManager.instance.createSyscatItemPagingList("z").loadPage(u, 1).getPageEntries();
        System.out.println(items);
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
	
	public void testCreateItem() throws SQLException {
		SmartpadCommon.initialize("localhost", null, "smartpad", "root", "", "../app-server/imaging/in-queue", "../app-server/imaging/root");
		IUser u = SmartpadCommon.partnerManager.createPrimaryUser("testCreateItem", "testCreateItem");
		//u.getBranch().setSystemCatalogId("z");
		//u.updateBranch();
        ICatalogItem item = u.getBranch().getRootCatalog().getCatalogItemPagingList().newEntryInstance(u);
        item.setBranchName("testCreateItem");
        item.setSystemCatalogId("z");
        item.setField(ICatalogField.F_NAME, "testCreateItem");
    	u.getBranch().getRootCatalog().getCatalogItemPagingList().put(u, item);
	}
	
	public void testCreateItemInManagedBranchName() throws SQLException {
		SmartpadCommon.initialize("localhost", null, "smartpad", "root", "", "../app-server/imaging/in-queue", "../app-server/imaging/root");
		 IUser u = SmartpadCommon.partnerManager.login("lotte", "lotte");
        ICatalogItem item = u.getBranch().getRootCatalog().getCatalogItemPagingList().newEntryInstance(u);
        item.setField(ICatalogField.F_NAME, "testCreateItemInManagedBranchName");
        u.getBranch().getRootCatalog().getCatalogItemPagingList().put(u, item);
	}
	
	public void testMissingBranchName() throws SQLException {
		SmartpadCommon.initialize("localhost", null, "smartpad", "root", "", "../app-server/imaging/in-queue", "../app-server/imaging/root");
		IUser u = SmartpadCommon.partnerManager.createPrimaryUser("testMissingBranchName", "testMissingBranchName");
		//u.getBranch().setSystemCatalogId("z");
		//u.updateBranch();
        ICatalogItem item = u.getBranch().getRootCatalog().getCatalogItemPagingList().newEntryInstance(u);
        item.setField(ICatalogField.F_NAME, "testMissingBranchName");
        try {
        	u.getBranch().getRootCatalog().getCatalogItemPagingList().put(u, item);
        	assert(false);
        } catch (Exception e) {
        	assert(true);
        }
	}
	
	public void testMissingSyscat() throws SQLException {
		SmartpadCommon.initialize("localhost", null, "smartpad", "root", "", "../app-server/imaging/in-queue", "../app-server/imaging/root");
		IUser u = SmartpadCommon.partnerManager.createPrimaryUser("testMissingSyscat", "testMissingSyscat");
		//u.getBranch().setSystemCatalogId("z");
		//u.updateBranch();
        ICatalogItem item = u.getBranch().getRootCatalog().getCatalogItemPagingList().newEntryInstance(u);
        item.setBranchName("testMissingSyscat");
        item.setField(ICatalogField.F_NAME, "testMissingSyscat");
        try {
        	u.getBranch().getRootCatalog().getCatalogItemPagingList().put(u, item);
        	assert(false);
        } catch (Exception e) {
        	assert(true);
        }
	}
}
