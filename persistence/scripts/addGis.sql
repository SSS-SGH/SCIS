
--CREATE EXTENSION postgis;
--CREATE EXTENSION postgis_topology;

alter table sgharchiv_dev_scis.ground_object add column coord geometry(point, 2056);

--update sgharchiv_dev_scis.ground_object set coord=ST_GeomFromText('POINT('||coord_east+2000000||' '||coord_north+1000000||')') ;

CREATE OR REPLACE view entrances_gis as
SELECT gobj.inventory_nr, kobj.name
     , gobj.coord
     , sobj.length, case when depth is not null and elevation is not null then (depth + elevation) 
				       when depth_and_elevation is not null then depth_and_elevation 
				       when depth is not null then depth 
				       else elevation end as depth_and_elevation_computed
     , sobj.system_nr 
FROM karst_object kobj 
JOIN ground_object gobj on gobj.id = kobj.id 
LEFT OUTER JOIN speleo_object sobj on sobj.id = gobj.speleo_object_id 
WHERE kobj.deleted = false 
;
