package com.jinnova.smartpad.drilling;

import java.math.BigDecimal;
import java.sql.SQLException;

interface DetailDriller {
	
	DrillResult drill(String clusterId, String targetId, BigDecimal lon, BigDecimal lat/*, int page, int size*/) throws SQLException;
}
