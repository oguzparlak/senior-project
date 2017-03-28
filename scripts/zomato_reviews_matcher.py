from firebase import firebase
import requests

def main():
    # Root reference of the Database
    database = firebase.FirebaseApplication('https://senior-app-1487777466334.firebaseio.com', None)

    zomato_restaurants = database.get('/nyc-zomato-external', None)

    for res_id, res_details in zomato_restaurants.iteritems():
        zomato_id = res_details['id']
        
        # If already matched skip
        if (database.get('nyc-zomato-reviews/', res_id) != None):
            pass
        else:
            # Make an API Call and push the reviews
            reviews_session = requests.Session()
            reviews_response = reviews_session.get('https://developers.zomato.com/api/v2.1/reviews?res_id=' + zomato_id,
                                        headers={'user-key': '8b000e3bd6d2a4434c926aeca9a040d0'})
            
            try:
                reviews_root = reviews_response.json()
            except Exception:
                print "Http 404, Not Found the review"
                continue
            
            try:
                reviews_arr = reviews_root['user_reviews']

                for review in reviews_arr:
                    review_details = review['review']
                    # Push the review
                    try:
                        database.post('/nyc-zomato-reviews/' + res_id, review_details)
                    except Exception:
                        print "Firebase error when post"
                        continue

            except Exception:
                print "Key Error"



if __name__ == '__main__':
    main()