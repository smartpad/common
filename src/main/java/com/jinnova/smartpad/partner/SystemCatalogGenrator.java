package com.jinnova.smartpad.partner;

import static com.jinnova.smartpad.partner.ICatalogField.*;
import static com.jinnova.smartpad.partner.ICatalogFieldType.*;
import static com.jinnova.smartpad.partner.IDetailManager.*;
import static com.jinnova.smartpad.partner.SmartpadCommon.*;

import java.sql.SQLException;

public class SystemCatalogGenrator {
	
	private static final boolean MANAGED = true;
	
	private static final boolean UNMANAGED = false;

	private static User systemUser;
	
	public static boolean createClusterTable = false;
	
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
		rootCat = createCat(rootCat, SYSTEM_CAT_ALL, UNMANAGED, "Tất cả");
		Catalog elec = createCat(rootCat, "elec", UNMANAGED, "Điện tử, điện gia dụng  & Máy tính");
		{
			Catalog elecComp = createCat(elec, "comp", UNMANAGED, "Điện thoại, máy tính");
			{
				createCat(elecComp, "phone", UNMANAGED, "Điện thoại di động");
				createCat(elecComp, "tablet", UNMANAGED, "Máy tính bảng");
				createCat(elecComp, "laptop", UNMANAGED, "Máy tính xách tay");
				createCat(elecComp, "desktop", UNMANAGED, "Máy tính để bàn");
				createCat(elecComp, "periph", UNMANAGED, "Thiết bị ngoại vi");
			}
			createCat(elec, "cam", UNMANAGED, "Máy ảnh & Máy quay phim");
			createCat(elec, "av", UNMANAGED, "Âm thanh & Hình ảnh");
			createCat(elec, "om", UNMANAGED, "Máy văn phòng");
			
			Catalog appliance = createCat(elec, "appliance", UNMANAGED, "Điện gia dụng");
			{
				i = 0;
				washerFields = new String[] {
							"wash_type", "wash_load", "max_rpm", "capacity", "water", "power", "sizes", "weight", "madein"};
				createCat(appliance, "washer", UNMANAGED, "Máy giặt", new Object[][] {
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
		Catalog fashion = createCat(rootCat, "fashion", UNMANAGED, "Quần áo, giầy dép & Trang sức");
		{
			i = 0;
			clothFields = new String[] {"material", "color", "style", "size", "madein", "sex"};
			createCat(fashion, "clothes", UNMANAGED, "Quần áo", new Object[][] {
				//{clothFields[i++], Text_Name, "Nhãn hiệu"},
				{clothFields[i++], Text_Name, "Chất liệu"},
				{clothFields[i++], Text_Name, "Màu"},
				{clothFields[i++], Text_Name, "Thể loại", SEGMENT_DISTINCT},
				{clothFields[i++], Text_Name, "Kích cỡ"},
				{clothFields[i++], Text_Name, "Xuất xứ"},
				{clothFields[i++], Text_Name, "Nam/nữ", SEGMENT_DISTINCT}
			});
		}
		
		createCat(rootCat, "health", UNMANAGED, "Y tế, sức khỏe & Làm đẹp");
		Catalog homeCat = createCat(rootCat, "household", UNMANAGED, "Gia đình & Trẻ em");
		{
			createCat(homeCat, "kids", UNMANAGED, "Trẻ em & Đồ chơi");
			createCat(homeCat, "fmcg", UNMANAGED, "Hàng tiêu dùng");
			
			i = 0;
			mattressFields = new String[] {"material", "width", "length", "thick", "madein"};
			Catalog mattressCat = createCat(homeCat, "mattress", UNMANAGED, "Nệm", new Object[][] {
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
			createCat(homeCat, "draps", UNMANAGED, "Draps", new Object[][] {
				//{mattressFields[i++], Text_Name, "Hãng sản xuất"},
				{mattressFields[i++], Text_Name, "Chất liệu", SEGMENT_DISTINCT},
				{mattressFields[i++], Int, "Rộng", SEGMENT_DISTINCT},
				{mattressFields[i++], Text_Name, "Xuất xứ", SEGMENT_DISTINCT}
			});
			
			i = 0;
			pillowFields = new String[] {"material", "style", "madein"};
			createCat(homeCat, "pillow", UNMANAGED, "Mềm / gối", new Object[][] {
				//{mattressFields[i++], Text_Name, "Hãng sản xuất"},
				{mattressFields[i++], Text_Name, "Chất liệu", SEGMENT_DISTINCT},
				{mattressFields[i++], Text_Name, "Loại", SEGMENT_DISTINCT},
				{mattressFields[i++], Text_Name, "Xuất xứ", SEGMENT_DISTINCT}
			});
		}

		Catalog entertain = createCat(rootCat, "entertain", UNMANAGED, "Thể thao, văn hóa, du lịch & ẩm thực");
		{
			Catalog foods = createCat(entertain, "foods", UNMANAGED, "Ẩm thực");
			{
				createCat(foods, "fastfoods", MANAGED, "Thức ăn nhanh");
			}
			
			createCat(entertain, "resort", UNMANAGED, "Khách sạn / resort");
			createCat(entertain, "event", UNMANAGED, "Sự kiện");
			createCat(entertain, "book", UNMANAGED, "Sách");
			createCat(entertain, "stationery", UNMANAGED, "Văn phòng phẩm");
		}
		
		//createmasterType(masterType, "Sách & Thiết bị trường học");
		createCat(rootCat, "edu", true, "Giáo dục, đào tạo & Việc làm");

		createCat(rootCat, "realestate", UNMANAGED, "Địa ốc & Bất động sản");
		createCat(rootCat, "transport", UNMANAGED, "Ô tô, vận tải & Công nghiệp");
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

}
