# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class Restaurant(scrapy.Item):
    # define the fields for your item here like:
    name = scrapy.Field()
    review_count = scrapy.Field()
    cuisines = scrapy.Field()
    reviews = scrapy.Field(serializer=list)
    address = scrapy.Field()
    opening_hours = scrapy.Field()
    phone_number = scrapy.Field()
    prices = scrapy.Field()
    rating = scrapy.Field()
    specifications = scrapy.Field()
    photos = scrapy.Field(serializer=list)