from firebase import firebase
import requests
from difflib import SequenceMatcher

# This script makes API Calls to Zomato to merge the data come from the TripAdvisor Crawler

# Get the similarity ratio between two strings
def get_similarity_ratio(a, b):
    return SequenceMatcher(None, a, b).ratio()

def main():
    # database root Reference
    database = firebase.FirebaseApplication('https://senior-app-1487777466334.firebaseio.com', None)
    
    # read one by one
    restaurants = database.get('/istanbul', None)
    
    for res_id, res_details in restaurants.iteritems():
        name = res_details['name']
        address = res_details['address']
        # If it doesn't match yet, make Zomato API Call
        if (database.get('istanbul-zomato-external/', res_id) == None):
            details_session = requests.Session()
            details_response = details_session.get('https://developers.zomato.com/api/v2.1/search?entity_id=280&entity_type=city&q=' + name.replace(" ", "%20"),
                        headers={'user-key': 'ff63af89c112c0bc627bf1fa51f2a10f'})

            
            try:
                json_root = details_response.json()
            except Exception as e:
                print "Couldn't find the data in the specified url"
                continue

            restaurants_arr = json_root['restaurants']

            # Match restaurant details
            for restaurant in restaurants_arr:
                restaurant_details = restaurant['restaurant']
                location = restaurant_details['location']
                # Pass the address into get_similarity_ratio function
                zomato_address = location['address']

                similarity = get_similarity_ratio(zomato_address, address)
                # 0.6 is considered as a match 
                if similarity > 0.6:
                    # there is a match push the data and break the loop
                    try:
                        database.put('/istanbul-zomato-external', res_id, restaurant_details)
                    except Exception:
                        print "Can't push to Firebase"
                        continue 

                    reviews_session = requests.Session()
                    reviews_response = reviews_session.get('https://developers.zomato.com/api/v2.1/reviews?res_id=' + res_id,
                                        headers={'user-key': 'ff63af89c112c0bc627bf1fa51f2a10f'})
                    
                    try:
                        reviews_root = reviews_response.json()
                    except Exception as e1:
                        print "Couldn't find the data in the specified url"
                        continue

                    try:
                        reviews_arr = reviews_root['user_reviews']

                        for review in reviews_arr:
                            review_details = review['review']
                            # push the review
                            try:
                                database.post('/istanbul-zomato-reviews/' + res_id, review_details)
                            except Exception:
                                print "Can't post to Firebase"
                                continue

                        break

                    except Exception:
                        print "Key Error"

if __name__ == '__main__':
    main()