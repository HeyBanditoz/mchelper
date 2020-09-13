UPDATE quotes SET quote_author = REGEXP_REPLACE(quote_author, '^-+', '') WHERE quote_author REGEXP '^-+';
