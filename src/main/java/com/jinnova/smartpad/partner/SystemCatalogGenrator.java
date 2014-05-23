package com.jinnova.smartpad.partner;

import static com.jinnova.smartpad.partner.ICatalogField.*;
import static com.jinnova.smartpad.partner.ICatalogFieldType.*;
import static com.jinnova.smartpad.partner.IDetailManager.*;

import java.sql.SQLException;

public class SystemCatalogGenrator {
	
	private static User systemUser;
	
	public static boolean createClusterTable = false;

	private static String[] clothFields;
	
	private static String[] washerFields;

	private static String[] mattressFields;

	private static String[] drapFields;

	private static String[] pillowFields;
	
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
		rootCat = createCat(rootCat, SYSTEM_CAT_ALL, false, "Tất cả");
		Catalog elec = createCat(rootCat, "elec", false, "Điện tử, điện gia dụng  & Máy tính");
		{
			Catalog elecComp = createCat(elec, "comp", false, "Điện thoại, máy tính");
			{
				createCat(elecComp, "phone", false, "Điện thoại di động");
				createCat(elecComp, "tablet", false, "Máy tính bảng");
				createCat(elecComp, "laptop", false, "Máy tính xách tay");
				createCat(elecComp, "desktop", false, "Máy tính để bàn");
				createCat(elecComp, "periph", false, "Thiết bị ngoại vi");
			}
			createCat(elec, "cam", false, "Máy ảnh & Máy quay phim");
			createCat(elec, "av", false, "Âm thanh & Hình ảnh");
			createCat(elec, "om", false, "Máy văn phòng");
			
			Catalog appliance = createCat(elec, "appliance", false, "Điện gia dụng");
			{
				i = 0;
				washerFields = new String[] {
							"wash_type", "wash_load", "max_rpm", "capacity", "water", "power", "sizes", "weight", "madein"};
				createCat(appliance, "washer", false, "Máy giặt", new Object[][] {
					//"wash_type", "wash_load", "max_rpm", "capacity", "water", "power", "sizes", "weight", "madein"
					//{washerFields[i++], Text_Name, "Hãng sản xuất"}, 
					{washerFields[i++], Text_ID, "Kiểu máy giặt", SEGMENT_DISTINCT},
					{washerFields[i++], Decimal, "Khối lượng giặt", SEGMENT_DISTINCT}, 
					{washerFields[i++], Int, "Tốc độ vắt tối đa (vòng/phút)"},
					{washerFields[i++], Int, "Dung tích thùng chứa (lít)"}, 
					{washerFields[i++], Int, "Lượng nước tiêu thụ (lít)"},
					{washerFields[i++], Int, "Điện năng tiêu thụ (W)"}, 
					{washerFields[i++], Text_Name, "Kích thước (mm)"},
					{washerFields[i++], Int, "Trọng lượng (kg)"}, {washerFields[i++], Text_Name, "Xuất xứ"}});
			}
		}
		
		//createCat(rootCat, "fmcg", "Hàng tiêu dùng");
		Catalog fashion = createCat(rootCat, "fashion", false, "Quần áo, giầy dép & Trang sức");
		{
			i = 0;
			clothFields = new String[] {"material", "color", "style", "size", "madein", "sex"};
			createCat(fashion, "clothes", false, "Quần áo", new Object[][] {
				//{clothFields[i++], Text_Name, "Nhãn hiệu"},
				{clothFields[i++], Text_Name, "Chất liệu"},
				{clothFields[i++], Text_Name, "Màu"},
				{clothFields[i++], Text_Name, "Thể loại", SEGMENT_DISTINCT},
				{clothFields[i++], Text_Name, "Kích cỡ"},
				{clothFields[i++], Text_Name, "Xuất xứ"},
				{clothFields[i++], Text_Name, "Nam/nữ", SEGMENT_DISTINCT}
			});
		}
		
		createCat(rootCat, "health", false, "Y tế, sức khỏe & Làm đẹp");
		Catalog homeCat = createCat(rootCat, "household", false, "Gia đình & Trẻ em");
		{
			createCat(homeCat, "kids", false, "Trẻ em & Đồ chơi");
			createCat(homeCat, "fmcg", false, "Hàng tiêu dùng");
			
			i = 0;
			mattressFields = new String[] {"material", "width", "length", "thick", "madein"};
			Catalog mattressCat = createCat(homeCat, "mattress", false, "Nệm", new Object[][] {
				//{mattressFields[i++], Text_Name, "Hãng sản xuất"},
				{mattressFields[i++], Text_Name, "Chất liệu", SEGMENT_DISTINCT},
				{mattressFields[i++], Int, "Rộng", SEGMENT_DISTINCT},
				{mattressFields[i++], Int, "Dài", SEGMENT_DISTINCT},
				{mattressFields[i++], Int, "Dày", SEGMENT_DISTINCT},
				{mattressFields[i++], Text_Name, "Xuất xứ", SEGMENT_DISTINCT}
			});
			//mattressCat.getCatalogSpec().setAttribute(ICatalogSpec.ATT_DISP_SEGMENTS_HIDDEN, "length, thick");
			mattressCat.getCatalogSpec().setAttribute(ICatalogSpec.ATT_DISP_DETAIL, 
					"<div>Chất liệu: <b>{material}</b></div><div>Xuất xứ: <b>{madein}</b></div>" +
					"<div>Quy cách: <b><a href='{segmentLink:length,width,thick}'>{-length} x {-width} x {-thick}</a></b></div>");
			homeCat.getSubCatalogPagingList().put(systemUser, mattressCat);
			
			i = 0;
			drapFields = new String[] {"material", "width", "madein"};
			createCat(homeCat, "draps", false, "Draps", new Object[][] {
				//{mattressFields[i++], Text_Name, "Hãng sản xuất"},
				{mattressFields[i++], Text_Name, "Chất liệu", SEGMENT_DISTINCT},
				{mattressFields[i++], Int, "Rộng", SEGMENT_DISTINCT},
				{mattressFields[i++], Text_Name, "Xuất xứ", SEGMENT_DISTINCT}
			});
			
			i = 0;
			pillowFields = new String[] {"material", "style", "madein"};
			createCat(homeCat, "pillow", false, "Mềm / gối", new Object[][] {
				//{mattressFields[i++], Text_Name, "Hãng sản xuất"},
				{mattressFields[i++], Text_Name, "Chất liệu", SEGMENT_DISTINCT},
				{mattressFields[i++], Text_Name, "Loại", SEGMENT_DISTINCT},
				{mattressFields[i++], Text_Name, "Xuất xứ", SEGMENT_DISTINCT}
			});
		}

		Catalog entertain = createCat(rootCat, "entertain", false, "Thể thao, văn hóa, du lịch & ẩm thực");
		{
			Catalog foods = createCat(entertain, "foods", true, "Ẩm thực");
			{
				createCat(foods, "fastfoods", true, "Thức ăn nhanh");
			}
			
			createCat(entertain, "resort", true, "Khách sạn / resort");
			createCat(entertain, "event", true, "Sự kiện");
			createCat(entertain, "book", false, "Sách");
			createCat(entertain, "stationery", false, "Văn phòng phẩm");
		}
		
		//createmasterType(masterType, "Sách & Thiết bị trường học");
		createCat(rootCat, "edu", true, "Giáo dục, đào tạo & Việc làm");

		createCat(rootCat, "realestate", false, "Địa ốc & Bất động sản");
		createCat(rootCat, "transport", false, "Ô tô, vận tải & Công nghiệp");
	}
	
	private static Catalog createCat(Catalog parentCat, String catId, boolean managed, String catName) throws SQLException {
		return createCat(parentCat, catId, managed, catName, null);
	}
	
	private static Catalog createCat(Catalog parentCat, String catId, boolean managed, String catName, Object[][] fieldIDTypeNames) throws SQLException {
		if (createClusterTable) {
			parentCat.setCreateCatItemClusterTable();
		}
		ICatalog cat = parentCat.getSubCatalogPagingList().newEntryInstance(systemUser);
		cat.setName(catName);
		cat.getCatalogSpec().setSpecId(catId); //table name
		((CatalogSpec) cat.getCatalogSpec()).setManaged(managed);
		Object[][] nameDesc = new Object[][] {
				{F_NAME, Text_Name, "Name"},
				{F_DESC, Text_Desc, "Description"}};
		createColumns(cat, nameDesc, false);
		if (fieldIDTypeNames != null) {
			createColumns(cat, fieldIDTypeNames, true);
		}
		parentCat.getSubCatalogPagingList().put(systemUser, cat);
		return (Catalog) cat;
	}
	
	private static void createColumns(ICatalog cat, Object[][] fieldIDTypeNames, boolean generateDetailsAtt) {

		StringBuffer displayDetails = new StringBuffer();
		for (Object[] oneIDTypeName : fieldIDTypeNames) {
			ICatalogField field = cat.getCatalogSpec().createField((String) oneIDTypeName[0]);
			field.setFieldType((ICatalogFieldType) oneIDTypeName[1]);
			field.setName((String) oneIDTypeName[2]);
			
			if (generateDetailsAtt) {
				//"<div>Chất liệu: <b>{material}</b></div>
				displayDetails.append("<div>" + oneIDTypeName[2] + ": <b>{" + oneIDTypeName[0] + "}</b></div>" + SmartpadCommon.SUBSTITUTION_SEP);
			}
			
			if (oneIDTypeName.length < 4) {
				continue;
			}
			field.setGroupingType((int) oneIDTypeName[3]);
		}
		
		if (generateDetailsAtt) {
			cat.getCatalogSpec().setAttribute(ICatalogSpec.ATT_DISP_DETAIL, displayDetails.toString());
		}
	}

	public static void createItems() throws SQLException {
		
		systemUser = PartnerManager.instance.systemUser;
		//"wash_type", "wash_load", "max_rpm", "capacity", "water", "power", "sizes", "weight", "madein"
		ICatalog cat = PartnerManager.instance.getSystemCatalog("z_elec_appliance_washer");
		createItem(cat, washerFields, "Sanyo ASW-D90VT", "SANYO", "Máy giặt lồng đứng", 9,   850, 65, 122, 160, "590 x 564 x 988", 41, "Việt Nam");
		createItem(cat, washerFields, "LG WFD8525DD",    "LG",    "Máy giặt lồng đứng", 8.5, 850, 79, 410, 0,   "540 x 910 x 540", 39, null);
		createItem(cat, washerFields, "Sanyo ASW-U850HT", "Sanyo","Máy giặt lồng nghiêng",8.5,840,62, 410, 0,   "589 x 620 x 988", 43, null);
		
		//"material", "length", "width", "thick", "madein"
		cat = PartnerManager.instance.getSystemCatalog("z_household_mattress");
		createItem(cat, mattressFields, "Nệm cao su Vạn Thành", "Vạn Thành", "Cao su", 160, 200, 5, "Việt Nam");
		createItem(cat, mattressFields, "Nệm cao su Liên Á Classic 180x200x10cm", "Liên Á", "Cao su", 180, 200, 10, "Việt Nam");
		createItem(cat, mattressFields, "Nệm cao su Venus Vạn Thành 100x200x10cm", "Vạn Thành", "Cao su", 100, 200, 10, "Việt Nam");
		createItem(cat, mattressFields, "Nệm bông ép Hàn Quốc Cuscino 140x200x5cm", "Cuscino", "Bông tấm PE ép", 140, 200, 5, "Hàn Quốc");
		
		//resort
		cat = PartnerManager.instance.getSystemCatalog("z_entertain_resort");
		createItem(cat, null, "Thiên Ý Resort", "Thiên Ý Resort");
		createItem(cat, null, "Vinpearl Resort", "Vinpearl Resort");
		
		//áo quần
		//"trademark", "material", "color", "style", "size", "madein", "sex"
		cat = PartnerManager.instance.getSystemCatalog("z_fashion_clothes");
		createItem(cat, clothFields, "Váy đầm dạ hội trẻ trung quyến rũ,đa dạng cho phụ nữ Việt Nam DV144",
				"DV", "Thun", "Đen/trắng", "Đầm liền", "Freesize", "Việt Nam", "Nữ");
		createItem(cat, clothFields, "Đầm body Ngọc Trinh 2 dây đơn giản sang trọng DV145", 
				"DV", "Thun", "Đỏ/trắng", "Đầm liền", "Freesize", "Việt Nam", "Nữ");
	}
	
	private static void createItem(ICatalog cat, String[] fieldNames, Object... data) throws SQLException {
		CatalogItem item = (CatalogItem) cat.getCatalogItemPagingList().newEntryInstance(systemUser);
		item.setField(F_NAME, String.valueOf(data[0]));
		item.setBranchName((String) data[1]);
		
		if (fieldNames == null) {
			return;
		}
		int offset = 2;
		for (int i = 0; i < fieldNames.length; i++) {
			item.setField(fieldNames[i], data[offset + i] == null ? null : String.valueOf(data[offset + i]));
		}
		cat.getCatalogItemPagingList().put(systemUser, item);
	}

}
