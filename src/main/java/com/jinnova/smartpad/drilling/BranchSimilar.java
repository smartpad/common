package com.jinnova.smartpad.drilling;

class BranchSimilar {
	
	/*public static void main(String[] args) throws SQLException {
		PartnerManager.initialize();
		SmartpadConnectionPool.instance.dataSource.getConnection().createStatement().executeUpdate("delete from similars_ver");
		SmartpadConnectionPool.instance.dataSource.getConnection().createStatement().executeUpdate("delete from similars");
		SmartpadConnectionPool.instance.dataSource.getConnection().createStatement().executeUpdate("insert into similars_ver (type_id, version) values ('branch', 'A')");
		generate("A");
	}*/
	
    /*public static void generate(String version) throws SQLException {

		Connection conn = null;
		PreparedStatement ps = null;
    	DbIterator<User> it = null;
    	try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into similars set target=?, json=?");
			
	    	it = PartnerManager.instance.iterateAllPrimaryUsers(conn);
	    	while (it.hasNext()) {
	    		User u = it.next();
	    		u.getStorePagingList().setPageSize(-1);
	    		IOperation[] stores = u.getStorePagingList().loadPage(u, 1).getPageItems();
	    		JsonArray ja = new JsonArray();
	    		for (IOperation one : stores) {
	    			JsonObject json = new JsonObject();
	    			json.addProperty("id", one.getId());
	    			json.addProperty("type", "store");
	    			json.addProperty("name", one.getName().getName());
	    			ja.add(json);
	    		}
	    		
	    		JsonObject branchJson = new JsonObject();
	    		branchJson.add("stores", ja);
	    		
	    		ps.setString(1, version + "branch" + u.branchId + 1);
	    		ps.setString(2, branchJson.toString());
	    		ps.executeUpdate();
	    	}
    	} finally {
    		if (it != null) {
    			it.close();
    		}
    		if (ps != null) {
    			ps.close();
    		}
			if (conn != null) {
				conn.close();
			}
    	}
    	
    }*/

	/*private static void generate(Operation store) {
		
	}*/
}
