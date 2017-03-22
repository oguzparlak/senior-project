from firebase import firebase
import requests
import time
from difflib import SequenceMatcher

GOOGLE_API_KEY = "AIzaSyBFf-DOlrxXAQuzKZDwX1fB6Zx8RAtw_TM"

def get_similarity_ratio(a, b):
    return SequenceMatcher(None, a, b).ratio()

def main():
    # database root Reference
    database = firebase.FirebaseApplication('https://senior-app-1487777466334.firebaseio.com', None)
    
    # get the reference of nyc
    restaurants = database.get('/new-york-city', None)

    for res_id, res_details in restaurants.iteritems():
        name = res_details['name']
        address = res_details['address']
        # Google Search API Call
        search_session = requests.Session()
        search_response = search_session.get('https://maps.googleapis.com/maps/api/place/textsearch/json?query='
         + name.replace(" ", "+") +'&language=en&key=' + GOOGLE_API_KEY)

        try:
            json_root = search_response.json()
        except Exception as e:
            print "Couldn't find the data in the specified url"
            continue

        results_arr = json_root['results']

        for result in results_arr:
            formatted_address = result['formatted_address']
            similarity = get_similarity_ratio(formatted_address, address)

            if similarity > 0.6:
                # get place_id and make detail request
                place_id = result['place_id']

                # push the related place_id so that it can be used to request API in Android Client.
                database.put('/nyc-google-places/', res_id, place_id)

                break
                

if __name__ == "__main__":
        main()