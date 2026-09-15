-- Phase 1.4: switch DEMO-DOOR products to MOCK protocol + close_door in thing model

UPDATE openbiz_product
SET protocol = 'MOCK',
    update_time = NOW()
WHERE product_code = 'DEMO-DOOR';

UPDATE openbiz_thing_model
SET model_json = '{"properties":[{"id":"door_status","type":"enum"}],"services":[{"id":"open_door","params":[]},{"id":"close_door","params":[]}],"events":[{"id":"door_open"}]}',
    update_time = NOW()
WHERE product_id IN (SELECT id FROM (SELECT id FROM openbiz_product WHERE product_code = 'DEMO-DOOR') t);
