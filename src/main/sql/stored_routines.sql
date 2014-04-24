

CREATE FUNCTION sp_dist (lon1 decimal(9,6), lat1 decimal(9,6), lon2 decimal(9,6), lat2 decimal(9,6))
    RETURNS decimal(9,6)

BEGIN
	return greatest(abs(lon1 - lon2), abs(lat1 - lat2));
END;

$$

CREATE FUNCTION sp_dist_grade (lon1 decimal(9,6), lat1 decimal(9,6), lon2 decimal(9,6), lat2 decimal(9,6))
    RETURNS varchar(1)

BEGIN
	declare d decimal(9,6);

	set d = greatest(abs(lon1 - lon2), abs(lat1 - lat2));

	if (d <= 0.001000) then
		--100m
		return 'a';
	end if;
	if (d <= 0.010000) then
		--1km
		return 'b';
	end if;
	if (d <= 0.100000) then
		--10km
		return 'c';
	end if;
	if (d <= 1.000000) then
		--100km
		return 'd';
	end if;
	if (d <= 10.000000) then
		--1000km
		return 'e';
	end if;

	return 'z';
END;

$$
