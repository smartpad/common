package com.jinnova.smartpad.partner;

import java.sql.SQLException;

public class SystemCatalogGenrator {
	
	private User systemUser;
	
	private boolean createClusterTable;
	
	/**
	 * constructor for reflection
	 */
	public SystemCatalogGenrator() {
		this.createClusterTable = false;
	}
	
	public SystemCatalogGenrator(boolean createClusterTable) {
		this.createClusterTable = createClusterTable;
	}
	
	public void generate() throws SQLException {
		
		//system catalog
		systemUser = PartnerManager.instance.systemUser;
		Catalog rootCat = PartnerManager.instance.getSystemRootCatalog();
		rootCat = createCat(rootCat, "z", "All");
		Catalog elec = createCat(rootCat, "elec", "Điện tử & Máy tính");
		{
			Catalog elecComp = createCat(elec, "comp", "Vi tính & viễn thông");
			{
				createCat(elecComp, "phone", "Điện thoại di động");
				createCat(elecComp, "tablet", "Máy tính bảng");
				createCat(elecComp, "laptop", "Máy tính xách tay");
				createCat(elecComp, "desktop", "Máy tính để bàn");
				createCat(elecComp, "item", "Thiết bị ngoại vi");
			}
			createCat(elec, "cam", "Máy ảnh & Máy quay phim");
			createCat(elec, "av", "Âm thanh & Hình ảnh");
		}
		Catalog appliance = createCat(rootCat, "appliance", "Điện gia dụng");
		{
			createCat(appliance, "washer", "Máy giặt", new Object[][] {
				{"manu", ICatalogFieldType.Text_Name, "Hãng sản xuất"},
				{"wash_type", ICatalogFieldType.Text_ID, "Kiểu máy giặt"},
				{"wash_load", ICatalogFieldType.Decimal, "Khối lượng giặt"},
				{"max_rpm", ICatalogFieldType.Int, "Tốc độ vắt tối đa (vòng/phút)"},
				{"capacity", ICatalogFieldType.Int, "Dung tích thùng chứa (lít)"},
				{"water", ICatalogFieldType.Int, "Lượng nước tiêu thụ (lít)"},
				{"power", ICatalogFieldType.Int, "Điện năng tiêu thụ (W)"},
				{"sizes", ICatalogFieldType.Text_Name, "Kích thước (mm)"},
				{"weight", ICatalogFieldType.Int, "Trọng lượng (kg)"},
				{"madein", ICatalogFieldType.Text_Name, "Xuất xứ"}});
		}
		
		createCat(rootCat, "office", "Máy văn phòng & Văn phòng phẩm");
		
		createCat(rootCat, "fmcg", "Hàng tiêu dùng");
		createCat(rootCat, "clothes", "Quần áo, giầy dép & Trang sức");
		createCat(rootCat, "kids", "Trẻ em & Đồ chơi");
		createCat(rootCat, "health", "Y tế, sức khỏe & Làm đẹp");
		createCat(rootCat, "home", "Gia đình & Nội ngoại thất");

		Catalog entertain = createCat(rootCat, "entertain", "Du lịch, giải trí & ẩm thực");
		{
			Catalog foods = createCat(entertain, "foods", "Foods");
			{
				createCat(foods, "fastfoods", "Thức ăn nhanh");
			}
		}
		createCat(rootCat, "sport", "Thể thao, văn hóa & Nghệ thuật");
		//createmasterType(masterType, "Sách & Thiết bị trường học");
		createCat(rootCat, "edu", "Giáo dục, đào tạo & Việc làm");
		
		createCat(rootCat, "transport", "Ô tô & Phương tiện vận tải");
		createCat(rootCat, "realestate", "Địa ốc & Bất động sản");
		createCat(rootCat, "industrials", "Công nghiệp, xây dựng & Doanh nghiệp");
	}
	
	private Catalog createCat(Catalog parentCat, String catId, String catName) throws SQLException {
		return createCat(parentCat, catId, catName, null);
	}
	
	private Catalog createCat(Catalog parentCat, String catId, String catName, Object[][] fieldIDTypeNames) throws SQLException {
		if (createClusterTable) {
			parentCat.setCreateCatItemClusterTable();
		}
		ICatalog cat = parentCat.getSubCatalogPagingList().newEntryInstance(systemUser);
		cat.setName(catName);
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
	
	private static void createColumns(ICatalog cat, Object[][] fieldIDTypeNames) {

		for (Object[] oneIDTypeName : fieldIDTypeNames) {
			ICatalogField field = cat.getCatalogSpec().createField();
			field.setId((String) oneIDTypeName[0]); //column name
			field.setFieldType((ICatalogFieldType) oneIDTypeName[1]);
			field.setName((String) oneIDTypeName[2]);
		}
	}

	public void createSystemItems() throws SQLException {
		systemUser = PartnerManager.instance.systemUser;
		ICatalog cat = PartnerManager.instance.getSystemCatalog("z_appliance_washer");
		createWasher(cat, "Sanyo ASW-D90VT", "SANYO", "Máy giặt lồng đứng", 9, 850, 65, 122, 160, "590 x 564 x 988", 41, "Việt Nam");
	}
	
	private void createWasher(ICatalog cat, Object... data) throws SQLException {
		ICatalogItem item = cat.getCatalogItemPagingList().newEntryInstance(systemUser);
		item.setField(ICatalogField.ID_NAME, String.valueOf(data[0]));
		cat.getCatalogItemPagingList().put(systemUser, item);
	}

}
