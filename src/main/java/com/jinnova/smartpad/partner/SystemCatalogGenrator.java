package com.jinnova.smartpad.partner;

import java.sql.SQLException;

class SystemCatalogGenrator {
	
	private User systemUser;
	
	void generate() throws SQLException {
		
		//system catalog
		systemUser = PartnerManager.instance.systemUser;
		Catalog rootCat = PartnerManager.instance.getSystemRootCatalog();
		createCat(rootCat, "elec", "Điện tử & Máy tính");
		createCat(rootCat, "appliance", "Điện gia dụng");
		createCat(rootCat, "office", "Máy văn phòng & Văn phòng phẩm");
		
		createCat(rootCat, "fmcg", "Hàng tiêu dùng");
		createCat(rootCat, "clothes", "Quần áo, giầy dép & Trang sức");
		createCat(rootCat, "kids", "Trẻ em & Đồ chơi");
		createCat(rootCat, "health", "Y tế, sức khỏe & Làm đẹp");
		createCat(rootCat, "home", "Gia đình & Nội ngoại thất");

		Catalog entertain = createCat(rootCat, "entertain", "Du lịch, giải trí & ẩm thực");
		{
			createCat(entertain, "foods", "Foods");
		}
		createCat(rootCat, "sport", "Thể thao, văn hóa & Nghệ thuật");
		//createmasterType(masterType, "Sách & Thiết bị trường học");
		createCat(rootCat, "edu", "Giáo dục, đào tạo & Việc làm");
		
		createCat(rootCat, "transport", "Ô tô & Phương tiện vận tải");
		createCat(rootCat, "realestate", "Địa ốc & Bất động sản");
		createCat(rootCat, "industrials", "Công nghiệp, xây dựng & Doanh nghiệp");
	}
	
	private Catalog createCat(Catalog parentCat, String catId, String catName, Object[]... fieldIDTypeNames) throws SQLException {
		ICatalog cat = parentCat.getSubCatalogPagingList().newEntryInstance(systemUser);
		cat.getName().setName(catName);
		cat.getCatalogSpec().setSpecId(catId); //table name
		Object[][] nameDesc = new Object[][] {
				{ICatalogField.ID_NAME, ICatalogFieldType.Text_Name, "Name"},
				{ICatalogField.ID_DESC, ICatalogFieldType.Text_Desc, "Description"}};
		createColumns(cat, nameDesc);
		if (fieldIDTypeNames != null) {
			createColumns(cat, fieldIDTypeNames);
		}
		parentCat.getSubCatalogPagingList().put(systemUser, cat);
		return (Catalog) cat;
	}
	
	private void createColumns(ICatalog cat, Object[][] fieldIDTypeNames) {

		for (Object[] oneIDTypeName : fieldIDTypeNames) {
			ICatalogField field = cat.getCatalogSpec().createField();
			field.setId((String) oneIDTypeName[0]); //column name
			field.setFieldType((ICatalogFieldType) oneIDTypeName[1]);
			field.setName((String) oneIDTypeName[2]);
		}
	}

}
