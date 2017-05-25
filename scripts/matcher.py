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
    restaurants = database.get('/london', None)
    
    for res_id, res_details in restaurants.iteritems():
        name = res_details['name']
        address = res_details['address']
        # If it doesn't match yet, make Zomato API Call
        if (database.get('london-zomato-external/', res_id) == None):
            details_session = requests.Session()
            details_response = details_session.get('https://developers.zomato.com/api/v2.1/search?entity_id=61&entity_type=city&q=' + name.replace(" ", "%20"),
                        headers={'user-key': 'ffc07a23459d9c09b21b5c4dc0ce55e4'})

            
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
                
                # Get Zomato id
                zomato_id = restaurant_details['id']

                # Pass the address into get_similarity_ratio function
                zomato_address = location['address']

                similarity = get_similarity_ratio(zomato_address, address)
                # 0.6 is considered as a match 
                if similarity > 0.6:
                    # there is a match push the data and break the loop
                    try:
                        database.put('/london-zomato-external', res_id, restaurant_details)
                    except Exception:
                        print "Can't push to Firebase"
                        continue 

                    break

if __name__ == '__main__':
    main()