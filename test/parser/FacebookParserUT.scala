package parser

import models.AnalysisType
import org.specs2.mutable.Specification
/**
 * Created by calvin-pc on 7/3/2015.
 */
class FacebookParserUT extends Specification{
  val parser = new FacebookParser
  val analysis = Seq(AnalysisType("1","coba",Seq()))
  "Facebook parser" should {
    "parse valid post" in {
      val valid =
        """
          |{
          |      "id": "10202147092822307_10204884419373760",
          |      "from": {
          |        "id": "10202147092822307",
          |        "name": "Joshua Bezaleel Abednego"
          |      },
          |      "to": {
          |        "data": [
          |          {
          |            "id": "10204561795746028",
          |            "name": "Fahmi Dumadi"
          |          },
          |          {
          |            "id": "839306026088315",
          |            "name": "Riady Sastra Kusuma"
          |          },
          |          {
          |            "id": "10204617696510183",
          |            "name": "Yusuf Rahmatullah"
          |          },
          |          {
          |            "id": "870686736280851",
          |            "name": "Muhammad Reza Irvanda"
          |          }
          |        ]
          |      },
          |      "message": "Fahmi Dumadi Riady Sastra Kusuma Dariel Valdano Yusuf Rahmatullah Muhammad Reza Irvanda",
          |      "message_tags": {
          |        "0": [
          |          {
          |            "id": "10204561795746028",
          |            "name": "Fahmi Dumadi",
          |            "type": "user",
          |            "offset": 0,
          |            "length": 12
          |          }
          |        ],
          |        "13": [
          |          {
          |            "id": "839306026088315",
          |            "name": "Riady Sastra Kusuma",
          |            "type": "user",
          |            "offset": 13,
          |            "length": 19
          |          }
          |        ],
          |        "48": [
          |          {
          |            "id": "10204617696510183",
          |            "name": "Yusuf Rahmatullah",
          |            "type": "user",
          |            "offset": 48,
          |            "length": 17
          |          }
          |        ],
          |        "66": [
          |          {
          |            "id": "870686736280851",
          |            "name": "Muhammad Reza Irvanda",
          |            "type": "user",
          |            "offset": 66,
          |            "length": 21
          |          }
          |        ]
          |      },
          |      "picture": "https://fbexternal-a.akamaihd.net/safe_image.php?d=AQAhvPp-HoV_nmeT&w=130&h=130&url=http%3A%2F%2Fi.ytimg.com%2Fvi%2FiQEM6TuKL0w%2Fmaxresdefault.jpg&cfs=1&sx=560&sy=0&sw=720&sh=720",
          |      "link": "https://www.youtube.com/watch?v=iQEM6TuKL0w",
          |      "source": "http://www.youtube.com/v/iQEM6TuKL0w?version=3&autohide=1&autoplay=1",
          |      "name": "CAPTCHA 2012 Gila ni rek",
          |      "caption": "youtube.com",
          |      "icon": "https://www.facebook.com/images/icons/post.gif",
          |      "actions": [
          |        {
          |          "name": "Comment",
          |          "link": "https://www.facebook.com/10202147092822307/posts/10204884419373760"
          |        },
          |        {
          |          "name": "Like",
          |          "link": "https://www.facebook.com/10202147092822307/posts/10204884419373760"
          |        },
          |        {
          |          "name": "Share",
          |          "link": "https://www.facebook.com/10202147092822307/posts/10204884419373760"
          |        }
          |      ],
          |      "privacy": {
          |        "value": "",
          |        "description": "",
          |        "friends": "",
          |        "allow": "",
          |        "deny": ""
          |      },
          |      "type": "video",
          |      "status_type": "shared_story",
          |      "created_time": "2015-07-05T17:12:25+0000",
          |      "updated_time": "2015-07-06T02:53:45+0000",
          |      "is_hidden": false,
          |      "is_expired": false,
          |      "likes": {
          |        "data": [
          |          {
          |            "id": "810527195664548",
          |            "name": "Hendro Triokta Brianto"
          |          },
          |          {
          |            "id": "10203719871151012",
          |            "name": "Kevin Maulana"
          |          },
          |          {
          |            "id": "1139581826058226",
          |            "name": "Teofebano Kristo"
          |          },
          |          {
          |            "id": "10202602690137152",
          |            "name": "Timothy Pratama"
          |          },
          |          {
          |            "id": "10204561795746028",
          |            "name": "Fahmi Dumadi"
          |          }
          |        ],
          |        "paging": {
          |          "cursors": {
          |            "after": "MTAyMDQ1NjE3OTU3NDYwMjg=",
          |            "before": "ODEwNTI3MTk1NjY0NTQ4"
          |          }
          |        }
          |      },
          |      "comments": {
          |        "data": [
          |          {
          |            "id": "10204884419373760_10204886747871971",
          |            "from": {
          |              "id": "10204561795746028",
          |              "name": "Fahmi Dumadi"
          |            },
          |            "message": "Haha di up lagi nih video jaman bego",
          |            "can_remove": false,
          |            "created_time": "2015-07-06T02:27:44+0000",
          |            "like_count": 0,
          |            "user_likes": false
          |          },
          |          {
          |            "id": "10204884419373760_10204886849874521",
          |            "from": {
          |              "id": "10202602690137152",
          |              "name": "Timothy Pratama"
          |            },
          |            "message": "hahah jaman2 sparta :)",
          |            "can_remove": false,
          |            "created_time": "2015-07-06T02:35:43+0000",
          |            "like_count": 0,
          |            "user_likes": false
          |          },
          |          {
          |            "id": "10204884419373760_10204886861834820",
          |            "from": {
          |              "id": "10203619557188227",
          |              "name": "Nisa Dian Rachmadi"
          |            },
          |            "message": "wah....",
          |            "can_remove": false,
          |            "created_time": "2015-07-06T02:39:38+0000",
          |            "like_count": 0,
          |            "user_likes": false
          |          },
          |          {
          |            "id": "10204884419373760_10204886906395934",
          |            "from": {
          |              "id": "10203719871151012",
          |              "name": "Kevin Maulana"
          |            },
          |            "message": "Gila besok ni video ultah ke-2",
          |            "can_remove": false,
          |            "created_time": "2015-07-06T02:53:45+0000",
          |            "like_count": 0,
          |            "user_likes": false
          |          }
          |        ],
          |        "paging": {
          |          "cursors": {
          |            "after": "WTI5dGJXVnVkRjlqZFhKemIzSTZNVEF5TURRNE9EWTVNRFl6T1RVNU16UTZNVFF6TmpFMU1USXlOUT09",
          |            "before": "WTI5dGJXVnVkRjlqZFhKemIzSTZNVEF5TURRNE9EWTNORGM0TnpFNU56RTZNVFF6TmpFME9UWTJOQT09"
          |          }
          |        }
          |      }
          |    }
        """.stripMargin
      val opt = parser.parse(valid,10,analysis,"2","3")
      opt.isDefined must_==  true
      val stream = opt.get
      stream._id must_== "3"
      stream.analysis must_== analysis
      stream.content must_== "CAPTCHA 2012 Gila ni rek"
      stream.date must_== "Mon Jul 06 00:12:25 ICT 2015"
      stream.id_provider must_== "2"
      stream.max_validasi must_== 10
      stream.total_validate must_== 0
    }

    "parse valid post (with name and description)" in {
      val valid =
        """
          |{
          |      "id": "863808483687441_863406037061019",
          |      "from": {
          |        "id": "863808483687441",
          |        "name": "Robert Tan"
          |      },
          |      "message": "I believe it will be one of the best leadership catalyst ever, go grab it guys :)",
          |      "story": "Robert Tan shared SCG Careers's photo.",
          |      "story_tags": {
          |        "0": [
          |          {
          |            "id": "863808483687441",
          |            "name": "Robert Tan",
          |            "type": "user",
          |            "offset": 0,
          |            "length": 10
          |          }
          |        ],
          |        "18": [
          |          {
          |            "id": "442500892488280",
          |            "name": "SCG Careers",
          |            "type": "page",
          |            "offset": 18,
          |            "length": 11
          |          }
          |        ],
          |        "32": [
          |          {
          |            "id": "880539918684373",
          |            "name": "",
          |            "offset": 32,
          |            "length": 5
          |          }
          |        ]
          |      },
          |      "picture": "https://scontent.xx.fbcdn.net/hphotos-xtf1/v/t1.0-9/s130x130/11667348_880539918684373_7566007251970207478_n.jpg?oh=38b75a70947524003a1109da14cfcb06&oe=56294914",
          |      "link": "https://www.facebook.com/SCGcareer/photos/a.447239738681062.101694.442500892488280/880539918684373/?type=1",
          |      "name": "SCG Careers",
          |      "description": "Coba-coba",
          |      "created_time": "2015-07-05T11:36:58+0000"
          |      }
        """.stripMargin
      val opt = parser.parse(valid,10,analysis,"2","3")
      opt.isDefined must_==  true
      val stream = opt.get
      stream._id must_== "3"
      stream.analysis must_== analysis
      stream.content must_== "SCG Careers"
    }
    "not parse invalid post" in {
      val valid =  "{}"
      val opt = parser.parse(valid,10,analysis,"2","3")
      opt.isDefined must_==  false
    }
  }
}
