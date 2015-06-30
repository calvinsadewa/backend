package parser

import models.AnalysisType
import org.specs2.mutable.Specification

/**
 * Created by calvin-pc on 6/30/2015.
 */
class TweetParserUT  extends Specification{
  val parser = new TwiterParser()
  val analysis = Seq(AnalysisType("1","coba",Seq()))
  "Tweet parser" should {
    "parse valid tweet" in {
      //source https://dev.twitter.com/rest/reference/get/statuses/show/%3Aid
      val valid =  "{\n  \"coordinates\": null,\n  \"favorited\": false,\n  \"truncated\": false,\n  \"created_at\": \"Wed Jun 06 20:07:10 +0000 2012\",\n  \"id_str\": \"210462857140252672\",\n  \"entities\": {\n    \"urls\": [\n      {\n        \"expanded_url\": \"https://dev.twitter.com/terms/display-guidelines\",\n        \"url\": \"https://t.co/Ed4omjYs\",\n        \"indices\": [\n          76,\n          97\n        ],\n        \"display_url\": \"dev.twitter.com/terms/display-\\u2026\"\n      }\n    ],\n    \"hashtags\": [\n      {\n        \"text\": \"Twitterbird\",\n        \"indices\": [\n          19,\n          31\n        ]\n      }\n    ],\n    \"user_mentions\": [\n \n    ]\n  },\n  \"in_reply_to_user_id_str\": null,\n  \"contributors\": [\n    14927800\n  ],\n  \"text\": \"Along with our new #Twitterbird, we've also updated our Display Guidelines: https://t.co/Ed4omjYs  ^JC\",\n  \"retweet_count\": 66,\n  \"in_reply_to_status_id_str\": null,\n  \"id\": 210462857140252672,\n  \"geo\": null,\n  \"retweeted\": true,\n  \"possibly_sensitive\": false,\n  \"in_reply_to_user_id\": null,\n  \"place\": null,\n  \"user\": {\n    \"profile_sidebar_fill_color\": \"DDEEF6\",\n    \"profile_sidebar_border_color\": \"C0DEED\",\n    \"profile_background_tile\": false,\n    \"name\": \"Twitter API\",\n    \"profile_image_url\": \"http://a0.twimg.com/profile_images/2284174872/7df3h38zabcvjylnyfe3_normal.png\",\n    \"created_at\": \"Wed May 23 06:01:13 +0000 2007\",\n    \"location\": \"San Francisco, CA\",\n    \"follow_request_sent\": false,\n    \"profile_link_color\": \"0084B4\",\n    \"is_translator\": false,\n    \"id_str\": \"6253282\",\n    \"entities\": {\n      \"url\": {\n        \"urls\": [\n          {\n            \"expanded_url\": null,\n            \"url\": \"http://dev.twitter.com\",\n            \"indices\": [\n              0,\n              22\n            ]\n          }\n        ]\n      },\n      \"description\": {\n        \"urls\": [\n \n        ]\n      }\n    },\n    \"default_profile\": true,\n    \"contributors_enabled\": true,\n    \"favourites_count\": 24,\n    \"url\": \"http://dev.twitter.com\",\n    \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/2284174872/7df3h38zabcvjylnyfe3_normal.png\",\n    \"utc_offset\": -28800,\n    \"id\": 6253282,\n    \"profile_use_background_image\": true,\n    \"listed_count\": 10774,\n    \"profile_text_color\": \"333333\",\n    \"lang\": \"en\",\n    \"followers_count\": 1212963,\n    \"protected\": false,\n    \"notifications\": null,\n    \"profile_background_image_url_https\": \"https://si0.twimg.com/images/themes/theme1/bg.png\",\n    \"profile_background_color\": \"C0DEED\",\n    \"verified\": true,\n    \"geo_enabled\": true,\n    \"time_zone\": \"Pacific Time (US & Canada)\",\n    \"description\": \"The Real Twitter API. I tweet about API changes, service issues and happily answer questions about Twitter and our API. Don't get an answer? It's on my website.\",\n    \"default_profile_image\": false,\n    \"profile_background_image_url\": \"http://a0.twimg.com/images/themes/theme1/bg.png\",\n    \"statuses_count\": 3333,\n    \"friends_count\": 31,\n    \"following\": true,\n    \"show_all_inline_media\": false,\n    \"screen_name\": \"twitterapi\"\n  },\n  \"in_reply_to_screen_name\": null,\n  \"source\": \"web\",\n  \"in_reply_to_status_id\": null\n}"
      val opt = parser.parse(valid,10,analysis,"2","3")
      opt.isDefined must_==  true
      val stream = opt.get
      stream._id must_== "3"
      stream.analysis must_== analysis
      stream.content must_== "Along with our new #Twitterbird, we've also updated our Display Guidelines: https://t.co/Ed4omjYs  ^JC"
      stream.date must_== "Wed Jun 06 20:07:10 +0000 2012"
      stream.id_provider must_== "2"
      stream.max_validasi must_== 10
      stream.total_validate must_== 0
    }
    "not parse invalid tweet" in {
      val valid =  "{}"
      val opt = parser.parse(valid,10,analysis,"2","3")
      opt.isDefined must_==  false
    }
  }
}
