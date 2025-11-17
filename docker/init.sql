CREATE TABLE SOLD (
                      ID UUID PRIMARY KEY,
                      VARIANT_ID UUID NOT NULL,
                      PRODUCT_ID UUID NOT NULL,
                      PRODUCT_NAME VARCHAR(255) NOT NULL,
                      PRICE DECIMAL(19, 4) NOT NULL,
                      QUANTITY INT NOT NULL,
                      MARGIN DECIMAL(19, 4),
                      STOCK_REMAINING INT,
                      DATE TIMESTAMP NOT NULL
);

CREATE INDEX idx_sold_variant_id ON SOLD (VARIANT_ID);

CREATE INDEX idx_sold_product_id ON SOLD (PRODUCT_ID);

CREATE INDEX idx_sold_product_name ON SOLD (PRODUCT_NAME);

CREATE INDEX idx_sold_date ON SOLD (DATE);



CREATE TABLE DELIVERY (
                          ID UUID PRIMARY KEY,
                          VARIANT_ID UUID NOT NULL,
                          DELIVERED_QUANTITY INT NOT NULL,
                          DELIVERED_AT TIMESTAMP NOT NULL
);

CREATE INDEX idx_delivery_variant_id ON DELIVERY (VARIANT_ID);

CREATE INDEX idx_delivery_delivered_at ON DELIVERY (DELIVERED_AT);
