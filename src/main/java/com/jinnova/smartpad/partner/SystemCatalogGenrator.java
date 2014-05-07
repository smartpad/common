package com.jinnova.smartpad.partner;

import static com.jinnova.smartpad.partner.ICatalogFieldType.*;

import java.sql.SQLException;

public class SystemCatalogGenrator {
	
	private static User systemUser;
	
	private static boolean createClusterTable;

	private static String[] clothFields;
	
	private static String[] washerFields;

	private static String[] mattressFields;
	
	/**
	 * constructor for reflection
	 */
	public SystemCatalogGenrator() {
		createClusterTable = false;
	}
	
	public SystemCatalogGenrator(boolean createClusterTable) {
		SystemCatalogGenrator.createClusterTable = createClusterTable;
	}
	
	public static void generate() throws SQLException {
		
		//system catalog
		systemUser = PartnerManager.instance.systemUser;
		int i;
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
			createCat(elec, "om", "Máy văn phòng");
		}
		Catalog appliance = createCat(rootCat, "appliance", "Điện gia dụng");
		{
			i = 0;
			washerFields = new String[] {
						"manu", "wash_type", "wash_load", "max_rpm", "capacity", "water", "power", "sizes", "weight", "madein"};
			createCat(appliance, "washer", "Máy giặt", new Object[][] {
				//"manu", "wash_type", "wash_load", "max_rpm", "capacity", "water", "power", "sizes", "weight", "madein"
				{washerFields[i++], Text_Name, "Hãng sản xuất"}, {washerFields[i++], Text_ID, "Kiểu máy giặt"},
				{washerFields[i++], Decimal, "Khối lượng giặt"}, {washerFields[i++], Int, "Tốc độ vắt tối đa (vòng/phút)"},
				{washerFields[i++], Int, "Dung tích thùng chứa (lít)"}, {washerFields[i++], Int, "Lượng nước tiêu thụ (lít)"},
				{washerFields[i++], Int, "Điện năng tiêu thụ (W)"}, {washerFields[i++], Text_Name, "Kích thước (mm)"},
				{washerFields[i++], Int, "Trọng lượng (kg)"}, {washerFields[i++], Text_Name, "Xuất xứ"}});
		}
		
		//createCat(rootCat, "office", "Máy văn phòng & Văn phòng phẩm");
		
		//createCat(rootCat, "fmcg", "Hàng tiêu dùng");
		Catalog fashion = createCat(rootCat, "fashion", "Quần áo, giầy dép & Trang sức");
		{
			i = 0;
			clothFields = new String[] {"trademark", "material", "color", "style", "size", "madein", "sex"};
			createCat(fashion, "clothes", "Quần áo", new Object[][] {
				{clothFields[i++], Text_Name, "Nhãn hiệu"},
				{clothFields[i++], Text_Name, "Chất liệu"},
				{clothFields[i++], Text_Name, "Màu"},
				{clothFields[i++], Text_Name, "Thể loại"},
				{clothFields[i++], Text_Name, "Kích cỡ"},
				{clothFields[i++], Text_Name, "Xuất xứ"},
				{clothFields[i++], Text_Name, "Nam/nữ"}
			});
		}
		createCat(rootCat, "kids", "Trẻ em & Đồ chơi");
		createCat(rootCat, "health", "Y tế, sức khỏe & Làm đẹp");
		Catalog homeCat = createCat(rootCat, "home", "Gia đình & Nội ngoại thất");
		{
			createCat(homeCat, "fmcg", "Hàng tiêu dùng");
			
			i = 0;
			mattressFields = new String[] {"manu", "material", "length", "width", "thick", "madein"};
			createCat(homeCat, "mattress", "Nệm", new Object[][] {
				{mattressFields[i++], Text_Name, "Hãng sản xuất"},
				{mattressFields[i++], Text_Name, "Chất liệu"},
				{mattressFields[i++], Int, "Dài"},
				{mattressFields[i++], Int, "Rộng"},
				{mattressFields[i++], Int, "Dày"},
				{mattressFields[i++], Text_Name, "Xuất xứ"}
			});
		}

		Catalog entertain = createCat(rootCat, "entertain", "Du lịch, giải trí & ẩm thực");
		{
			Catalog foods = createCat(entertain, "foods", "Foods");
			{
				createCat(foods, "fastfoods", "Thức ăn nhanh");
			}
			
			createCat(entertain, "resort", "Khách sạn / resort");
			createCat(entertain, "event", "Sự kiện");
		}
		
		Catalog sportCulture = createCat(rootCat, "sport", "Thể thao, văn hóa & Nghệ thuật");
		{
			createCat(sportCulture, "book", "Sách");
			createCat(sportCulture, "stanary", "Văn phòng phẩm");
		}
		
		//createmasterType(masterType, "Sách & Thiết bị trường học");
		createCat(rootCat, "edu", "Giáo dục, đào tạo & Việc làm");
		
		createCat(rootCat, "transport", "Ô tô & Phương tiện vận tải");
		createCat(rootCat, "realestate", "Địa ốc & Bất động sản");
		createCat(rootCat, "industrials", "Công nghiệp, xây dựng & Doanh nghiệp");
	}
	
	private static Catalog createCat(Catalog parentCat, String catId, String catName) throws SQLException {
		return createCat(parentCat, catId, catName, null);
	}
	
	private static Catalog createCat(Catalog parentCat, String catId, String catName, Object[][] fieldIDTypeNames) throws SQLException {
		if (createClusterTable) {
			parentCat.setCreateCatItemClusterTable();
		}
		ICatalog cat = parentCat.getSubCatalogPagingList().newEntryInstance(systemUser);
		cat.setName(catName);
		cat.getCatalogSpec().setSpecId(catId); //table name
		Object[][] nameDesc = new Object[][] {
				{ICatalogField.ID_NAME, Text_Name, "Name"},
				{ICatalogField.ID_DESC, Text_Desc, "Description"}};
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

	public static void createSystemItems() throws SQLException {
		
		systemUser = PartnerManager.instance.systemUser;
		//"manu", "wash_type", "wash_load", "max_rpm", "capacity", "water", "power", "sizes", "weight", "madein"
		ICatalog cat = PartnerManager.instance.getSystemCatalog("z_appliance_washer");
		createItem(cat, washerFields, "Sanyo ASW-D90VT", "SANYO", "Máy giặt lồng đứng", 9,   850, 65, 122, 160, "590 x 564 x 988", 41, "Việt Nam");
		createItem(cat, washerFields, "LG WFD8525DD",    "LG",    "Máy giặt lồng đứng", 8.5, 850, 79, 410, 0,   "540 x 910 x 540", 39, null);
		createItem(cat, washerFields, "Sanyo ASW-U850HT", "Sanyo","Máy giặt lồng nghiêng",8.5,840,62, 410, 0,   "589 x 620 x 988", 43, null);
		
		//"manu", "material", "length", "width", "thick", "madein"
		cat = PartnerManager.instance.getSystemCatalog("z_home_mattress");
		createItem(cat, mattressFields, "Nệm cao su Vạn Thành", "Vạn Thành", "Cao su", 160, 200, 5, "Việt Nam");
		createItem(cat, mattressFields, "Nệm cao su Liên Á Classic 180x200x10cm", "Liên Á", "Cao su", 180, 200, 10, "Việt Nam");
		createItem(cat, mattressFields, "Nệm cao su Venus Vạn Thành 100x200x10cm", "Vạn Thành", "Cao su", 100, 200, 10, "Việt Nam");
		createItem(cat, mattressFields, "Nệm bông ép Hàn Quốc Cuscino 140x200x5cm", null, "Bông tấm PE ép", 140, 200, 5, "Hàn Quốc");
		
		//resort
		cat = PartnerManager.instance.getSystemCatalog("z_entertain_resort");
		createItem(cat, null, "Thiên Ý Resort");
		createItem(cat, null, "Vinpearl Resort");
		
		//áo quần
		//"trademark", "material", "color", "style", "size", "madein", "sex"
		cat = PartnerManager.instance.getSystemCatalog("z_fashion_clothes");
		createItem(cat, clothFields, "Váy đầm dạ hội trẻ trung quyến rũ,đa dạng cho phụ nữ Việt Nam DV144",
				null, "Thun", "Đen/trắng", "Đầm liền", "Freesize", "Việt Nam", "Nữ");
		createItem(cat, clothFields, "Đầm body Ngọc Trinh 2 dây đơn giản sang trọng DV145", 
				null, "Thun", "Đỏ/trắng", "Đầm liền", "Freesize", "Việt Nam", "Nữ");
	}
	
	private static void createItem(ICatalog cat, String[] fieldNames, Object... data) throws SQLException {
		ICatalogItem item = cat.getCatalogItemPagingList().newEntryInstance(systemUser);
		item.setField(ICatalogField.ID_NAME, String.valueOf(data[0]));
		
		if (fieldNames == null) {
			return;
		}
		int offset = 1;
		for (int i = 0; i < fieldNames.length; i++) {
			item.setField(fieldNames[i], String.valueOf(data[offset + i]));
		}
		cat.getCatalogItemPagingList().put(systemUser, item);
	}

}
