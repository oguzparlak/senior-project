import scrapy
from scrapy.item import Item
from Crawler.items import Restaurant

import sys

from firebase import firebase

class TripAdvisorSpider(scrapy.Spider):
    name = 'ta_spider'
    start_urls = ['https://www.tripadvisor.com.tr/Restaurants-g186338-London_England.html']

    def parse(self, response):

        reload(sys)
        sys.setdefaultencoding('utf-8')

        for item in response.css('div.shortSellDetails'):
            restaurant = Restaurant()
            title = item.css('a.property_title::text').extract_first()
            cuisines = item.css('div.cuisines a::text').extract()
            review_count = item.css('span.reviewCount a::text').extract_first()
            rating_str = str(item.css('img.sprite-ratings::attr(alt)').extract_first())
            rating = str.split(rating_str)[0]

            restaurant['name'] = title
            restaurant['rating'] = rating
            restaurant['cuisines'] = cuisines
            restaurant['review_count'] = review_count

            details = scrapy.Request('https://www.tripadvisor.com' + item.css('span.reviewCount a::attr(href)').extract_first(),
                                     callback=self.parse_details, meta={'item' : restaurant})

            yield details

        next_page = response.css('a.nav.next.rndBtn.ui_button.primary.taLnk::attr(href)').extract_first()
        if next_page is not None:
            next_page = response.urljoin(next_page)
            yield scrapy.Request(next_page, callback=self.parse)


    def parse_details(self, response):
        review_list = []
        restaurant = response.meta['item']

        #Address and Specifications
        #Address and Phone Number
        phone_number = response.css('div.fl.notLast div.fl.phoneNumber::text').extract_first()

        for address_details in response.css('div.details_tab ul.detailsContent'):
            full_address = address_details.css('span.format_address span.street-address::text').extract_first() + ', ' + address_details.css('span.format_address span.locality::text').extract_first()  
            restaurant['address'] = full_address
        
        restaurant['phone_number'] = phone_number
        
        #Price Interval
        price_interval = response.css('div.details_tab div.row div.content span::text').extract_first()

        #Loop through the div contents
        for detail in response.css('div.details_tab'):
            dishes = detail.css('div.row div.content::text')[3].extract()
            properties = detail.css('div.row div.content::text')[4].extract()

        restaurant['specifications'] = dict(dishes=dishes, price_interval=price_interval, properties=properties)

        #Reviews Loop
        for review in response.css('div.reviewSelector'):
            title = review.css('div.quote span.noQuotes::text').extract_first()
            entry = review.css('p.partial_entry::text').extract_first()
            user_name = review.css('div.username.mo span::text').extract_first()
            user_location = review.css('div.member_info div.location::text').extract_first()
            provider = 'TripAdvisor'

            #Add fields
            review_dict = dict(title=title, entry=entry, user_name=user_name, user_location=user_location, provider=provider)
            review_list.append(review_dict)

        #END LOOP
        restaurant['reviews'] = review_list
        #return restaurant
        
        #Parse Photo URLS
        photo_href = response.css('div.flexible_photo_cell.hoverHighlight.restaurant_first a::attr(href)').extract_first()
        photo_url = 'https://www.tripadvisor.com' + str(photo_href)

        photos = scrapy.Request(photo_url, callback=self.parse_images, meta={'item' : restaurant})
        yield photos


    def parse_images(self, response):
        photo_urls = []
        restaurant = response.meta['item']

        #Photos Loop
        for photo in response.css('div.photoBox div.imgBx'):
            photo_url = photo.css('img::attr(src)').extract_first()
            photo_urls.append(photo_url)

        restaurant['photos'] = photo_urls

        # Just before return push the data into Firebase
        # Root reference of the database
        database = firebase.FirebaseApplication('https://senior-app-1487777466334.firebaseio.com', None)

        photos = []

        # push photos
        for photo in restaurant['photos']:
            photos.append(photo)

        restaurant_data = {
            "name": restaurant['name'],
            "rating": restaurant['rating'],
            "cuisines": restaurant['cuisines'],
            "photos": photos,
            "reviewCount": restaurant['review_count'],
            "phoneNumber": restaurant['phone_number'],
            "address": restaurant['address'],
            "specs": restaurant['specifications']
        }

        res_id = database.post('/london', restaurant_data)['name']

        # push reviews
        for review in restaurant['reviews']:
            database.post('/reviews/' + res_id, review)

        return restaurant
