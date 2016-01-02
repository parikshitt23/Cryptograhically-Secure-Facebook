
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor._
import akka.actor.Actor
import akka.actor.ActorDSL._
import scala.concurrent.Future
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter
import scala.concurrent.Await
import spray.routing.HttpService
import spray.http.MediaTypes
import akka.pattern.ask
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpServiceActor
import spray.http.StatusCodes._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.concurrent.duration._
import spray.json.DefaultJsonProtocol
import java.io._
import spray.http.{ MediaTypes, BodyPart, MultipartFormData }
import org.apache.commons.codec.binary
import org.apache.commons.codec.binary.Base64
import java.io.FileOutputStream
import akka.util.Timeout
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import javax.crypto.Cipher;
import java.security.MessageDigest
import org.apache.commons.lang.SerializationUtils
//import spray.json.DefaultJsonProtocol._

case class User(userId: Int, name: String, gender: String, userPublicKey: String)
case class Page(pageId: Int, pageName: String, likes: Int, pagePublicKey: String)
case class UserPost(postId: Int, admin_creator: Int, post: String)
case class UserEncryptedPost(postId: Int, admin_creator: Int, post: String, key: String)
case class Post(postId: Int, admin_creator: Int, post: String)
case class FriendList(userId: Int, friendList: List[User])
case class FriendRequestsList(userId: Int, requestsList: List[User])
case class userImageJson(userId: String, pictureId: String, Image: String, key : String)
case class pageImageJson(pageId: String, pictureId: String, Image: String)
case class statistics(pagePost_Count: Int, userPost_Count: Int, Total_Number_of_Posts: Int, picture_Post_count: Int, Number_of_friendRequests_sent: Int)
case class setPageList(userList: scala.collection.mutable.Map[Int, Page])
case class setUserList(userListFrom: scala.collection.mutable.Map[Int, User])
case class updatePageLikeList(pageId: Int, userId: Int, pageList: scala.collection.mutable.Map[Int, Page], pageLikeList: scala.collection.mutable.Map[Int, List[Int]])
//*********************************************************************************************************************
case class ObjectForLike(pageList: scala.collection.mutable.Map[Int, Page], pageLikeList: scala.collection.mutable.Map[Int, List[Int]])
case class pagePost(pageId: Int, postId: Int, post: String, pagePostList: scala.collection.mutable.Map[Int, List[Post]])
case class userPostMethod(userId: Int, fromUser: Int, postId: Int, post: String, friendList: scala.collection.mutable.Map[Int, List[User]], userPostList: scala.collection.mutable.Map[Int, List[UserPost]])
case class userEncryptedPostMethod(userId: Int, fromUser: Int, postId: Int, post: String, key: String, friendList: scala.collection.mutable.Map[Int, List[User]], userEncryptedPostList: scala.collection.mutable.Map[Int, List[UserEncryptedPost]])
case class updateUnlike(pageId: Int, userId: Int, pageList: scala.collection.mutable.Map[Int, Page], pageLikeList: scala.collection.mutable.Map[Int, List[Int]])
case class deletePagePost(pageId: Int, postId: Int, pagePostList: scala.collection.mutable.Map[Int, List[Post]])
case class deleteUserPost(userId: Int, fromUser: Int, postId: Int, userPostList: scala.collection.mutable.Map[Int, List[UserPost]])
case class setUserPicture(imageJson: userImageJson, postUserPictureList: scala.collection.mutable.Map[Int, List[userImageJson]])
case class setPagePicture(imageJson: pageImageJson, postPagePictureList: scala.collection.mutable.Map[Int, List[pageImageJson]])
case class friendRequest(userId: Int, friendId: Int, friendRequestsList: scala.collection.mutable.Map[Int, List[User]], userList: scala.collection.mutable.Map[Int, User])
case class approveDeclineRequest(userId: Int, friendId: Int, decision: Boolean, friendList: scala.collection.mutable.Map[Int, List[User]], friendRequestsList: scala.collection.mutable.Map[Int, List[User]], userList: scala.collection.mutable.Map[Int, User])
case class ObjectForFriend(friendList: scala.collection.mutable.Map[Int, List[User]], friendRequestsList: scala.collection.mutable.Map[Int, List[User]], userList: scala.collection.mutable.Map[Int, User])
case class pageLikeUnlikeJson(pageId: Int, userId: Int)
case class pagePostJson(pageId: Int, postId: Int, post: String)
case class deletePostJson(pageId: Int, postId: Int)
case class friendRequestJson(userId: Int, friendId: Int)
case class approveDeclineRequestJson(userId: Int, friendId: Int, decision: Boolean)
case class userPostJson(userId: Int, fromUser: Int, postId: Int, post: String)
case class userEncryptedPostJson(userId: Int, fromUser: Int, postId: Int, post: String, key: String)
case class deleteUserPostJson(userId: Int, fromUser: Int, postId: Int)

object deleteUserPostJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat3(deleteUserPostJson.apply)
}

object userEncryptedPostJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat5(userEncryptedPostJson.apply)
}

object userPostJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat4(userPostJson.apply)
}
object approveDeclineRequestJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat3(approveDeclineRequestJson.apply)
}

object friendRequestJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat2(friendRequestJson.apply)
}

object deletePostJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat2(deletePostJson.apply)
}

object pagePostJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat3(pagePostJson.apply)
}

object userImageJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat4(userImageJson.apply)
}

object pageLikeUnlikeJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat2(pageLikeUnlikeJson.apply)
}

object pageImageJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat3(pageImageJson.apply)
}

object User extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat4(User.apply)
}
object Page extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat4(Page.apply)
}
object UserPost extends DefaultJsonProtocol {
  implicit var pagePostFormat = jsonFormat3(UserPost.apply)
}
object UserEncryptedPost extends DefaultJsonProtocol {
  implicit var pagePostFormat = jsonFormat4(UserEncryptedPost.apply)
}
object Post extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat3(Post.apply)
}
object FriendList extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat2(FriendList.apply)
}
object FriendRequestsList extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat2(FriendRequestsList.apply)
}
object statistics extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat5(statistics.apply)
}

class ServerActor extends HttpServiceActor {
  override def actorRefFactory = context
  implicit def executionContext = actorRefFactory.dispatcher

  var userList = scala.collection.mutable.Map[Int, User]()
  var pageList = scala.collection.mutable.Map[Int, Page]()
  var userPublicKeyList = scala.collection.mutable.Map[Int, PublicKey]()
  var pagePublicKeyList = scala.collection.mutable.Map[Int, PublicKey]()
  var pageLikeList = scala.collection.mutable.Map[Int, List[Int]]()
  // var pagePostList1 = scala.collection.mutable.Map[Int, PagePost]()
  var pagePostList = scala.collection.mutable.Map[Int, List[Post]]()
  var userPostList = scala.collection.mutable.Map[Int, List[UserPost]]()
  var userEncryptedPostList = scala.collection.mutable.Map[Int, List[UserEncryptedPost]]()
  var friendList = scala.collection.mutable.Map[Int, List[User]]()
  var friendRequestsList = scala.collection.mutable.Map[Int, List[User]]()
  var postUserPictureList = scala.collection.mutable.Map[Int, List[userImageJson]]()
  var postPagePictureList = scala.collection.mutable.Map[Int, List[pageImageJson]]()
  var tokens = ArrayBuffer[String]()
  var pagePostCounter = 0
  var userPostCounter = 0
  var totalPostCounter = 0
  var picturePostCount = 0
  var friendRequestSent = 0
  var stat: statistics = null
  val worker = actorRefFactory.actorOf(
    Props[ServerWorker].withRouter(RoundRobinRouter(nrOfInstances = 1000)), name = "worker")

  val routes = {
    respondWithMediaType(MediaTypes.`application/json`) {
      path("user" / IntNumber) { (userId) =>
        get {
          headerValueByName("token") { token =>
            if (tokens.contains(token)) {
              userList.get(userId) match {
                case Some(userRoute) => complete(userRoute)
                case None            => complete(NotFound -> s"No user with id $userId was found!")
              }
            } else {
              println("NOT LOGGED IN!!!!!")
              complete(NotFound -> s"You are not logged in!!!")
            }
          }
        }
      }
    } ~
      post {
        path("registerUser") {
          entity(as[User]) { (userRegisterJson) =>
          //parameters("userId".as[Int], "name".as[String], "gender".as[String]) { (userId, name, gender) =>
            userList += userRegisterJson.userId.toInt -> User(userRegisterJson.userId, userRegisterJson.name, userRegisterJson.gender, userRegisterJson.userPublicKey)
            var decodedBase64String = Base64.decodeBase64(userRegisterJson.userPublicKey.getBytes())
            var userPublicKey : java.security.PublicKey =  SerializationUtils.deserialize(decodedBase64String).asInstanceOf[java.security.PublicKey]
            userPublicKeyList += userRegisterJson.userId.toInt  -> userPublicKey
            complete {
              "OK"
            }
          }
        }
      } ~ post {
        path("registerPage") {
          entity(as[Page]) { (pageRegisterJson) =>
          //parameters("pageId".as[Int], "pageName".as[String]) { (pageId, pageName) =>
            pageList += pageRegisterJson.pageId -> Page(pageRegisterJson.pageId, pageRegisterJson.pageName, pageRegisterJson.likes, pageRegisterJson.pagePublicKey)
            var decodedBase64String = Base64.decodeBase64(pageRegisterJson.pagePublicKey.getBytes())
            var pagePublicKey : java.security.PublicKey =  SerializationUtils.deserialize(decodedBase64String).asInstanceOf[java.security.PublicKey]
            pagePublicKeyList += pageRegisterJson.pageId.toInt  -> pagePublicKey
            complete {
              "OK"
            }
          }
          }~ post {
        path("loginUser") {
          entity(as[User]) { (userRegisterJson) =>
            var randomToken = Random.alphanumeric.take(10).mkString
             var decodedBase64String = Base64.decodeBase64(userRegisterJson.userPublicKey.getBytes())
            var userPublicKey : java.security.PublicKey =  SerializationUtils.deserialize(decodedBase64String).asInstanceOf[java.security.PublicKey]
            var token = encryptRSA(randomToken, userPublicKey)
            tokens += randomToken
            //println("Random token "+randomToken)
            complete {
              token
            }
          }
        }
        } ~ post {
        path("loginVerified") {
              headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                println("New User Logged In!!!!")
                complete {
                  "You are logged in"
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }
          
        }
        }~ post {
        path("loginPage") {
          entity(as[Page]) { (pageRegisterJson) =>
            var randomToken = Random.alphanumeric.take(10).mkString
             var decodedBase64String = Base64.decodeBase64(pageRegisterJson.pagePublicKey.getBytes())
            var pagePublicKey : java.security.PublicKey =  SerializationUtils.deserialize(decodedBase64String).asInstanceOf[java.security.PublicKey]
            var token = encryptRSA(randomToken, pagePublicKey)
            tokens += randomToken
            //println("Random token "+randomToken)
            //println(randomToken.getBytes())
            complete {
              token
            }
          }
        }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("page" / IntNumber) { (pageId) =>
          get {
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                pageList.get(pageId) match {
                  case Some(userRoute) => complete(userRoute)
                  case None            => complete(NotFound -> s"No page with id $pageId was found!")
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ post {
        path("likePage") {
          entity(as[pageLikeUnlikeJson]) { (pageLikeJson) =>
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                implicit val timeout = Timeout(5 seconds)
                var future: Future[ObjectForLike] = ask(worker, updatePageLikeList(pageLikeJson.pageId, pageLikeJson.userId, pageList, pageLikeList)).mapTo[ObjectForLike]
                future onSuccess {
                  case result => {
                    pageList = result.pageList
                    pageLikeList = result.pageLikeList
                  }

                }
                complete {
                  "OK"
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ post {
        path("unlikePage") {
          entity(as[pageLikeUnlikeJson]) { (pageUnlikeJson) =>
            // parameters("pageId".as[Int], "userId".as[Int]) { (pageId, userId) =>
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                implicit val timeout = Timeout(5 seconds)
                var future: Future[ObjectForLike] = ask(worker, updateUnlike(pageUnlikeJson.pageId, pageUnlikeJson.userId, pageList, pageLikeList)).mapTo[ObjectForLike]
                future onSuccess {
                  case result => {
                    pageList = result.pageList
                    pageLikeList = result.pageLikeList
                  }
                }
                complete {
                  "OK"
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ post {
        path("pagePost") {

          entity(as[pagePostJson]) { (pagePostJson) =>
            // parameters("pageId".as[Int], "post".as[String]) { (pageId, post) =>
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                implicit val timeout = Timeout(5 seconds)
                var future: Future[scala.collection.mutable.Map[Int, List[Post]]] = ask(worker, pagePost(pagePostJson.pageId, pagePostJson.postId, pagePostJson.post, pagePostList)).mapTo[scala.collection.mutable.Map[Int, List[Post]]]
                future onSuccess {
                  case result => {
                    pagePostCounter = pagePostCounter + 1
                    pagePostList = result
                  }

                }
                complete {
                  "OK"
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("page" / IntNumber / "feed") { (pageId) =>
          get {
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                pagePostList.get(pageId) match {
                  case Some(userRoute) => complete(userRoute)
                  case None            => complete(NotFound -> s"No posts for page id $pageId was found!")
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ post {
        path("deletePost") {
          entity(as[deletePostJson]) { (deletePostJson) =>
            //parameters("pageId".as[Int], "postId".as[Int]) { (pageId, postId) =>
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                implicit val timeout = Timeout(5 seconds)
                var future: Future[scala.collection.mutable.Map[Int, List[Post]]] = ask(worker, deletePagePost(deletePostJson.pageId, deletePostJson.postId, pagePostList)).mapTo[scala.collection.mutable.Map[Int, List[Post]]]
                future onSuccess {
                  case result => {
                    userPostCounter = userPostCounter + 1
                    totalPostCounter = totalPostCounter + 1
                    //println(userPostCounter)
                    pagePostList = result
                  }
                }
                complete {
                  "OK"
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("user" / IntNumber / "friendsList") { (userId) =>
          get {
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                friendList.get(userId) match {
                  case Some(userRoute) => complete(userRoute)
                  case None            => complete(NotFound -> s"No friends for user id $userId was founds!")
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }
          }

        } ~ respondWithMediaType(MediaTypes.`application/json`) {
          path("user" / IntNumber / "friendRequestsList") { (userId) =>
            get {
              headerValueByName("token") { token =>
                if (tokens.contains(token)) {
                  friendRequestsList.get(userId) match {
                    case Some(userRoute) => complete(userRoute)
                    case None            => complete(NotFound -> s"No friends REQUESTS for user id $userId was founds!")
                  }
                } else {
                  println("NOT LOGGED IN!!!!!")
                  complete(NotFound -> s"You are not logged in!!!")
                }
              }
            }
          }
        }
      } ~ post {
        path("friendRequest") {
          entity(as[friendRequestJson]) { (friendRequestJson) =>
            // parameters("userId".as[Int], "friendId".as[Int]) { (userId, friendId) =>
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                implicit val timeout = Timeout(5 seconds)
                var future: Future[scala.collection.mutable.Map[Int, List[User]]] = ask(worker, friendRequest(friendRequestJson.userId, friendRequestJson.friendId, friendRequestsList, userList)).mapTo[scala.collection.mutable.Map[Int, List[User]]]
                future onSuccess {
                  case result => {
                    friendRequestSent = friendRequestSent + 1
                    friendRequestsList = result
                  }
                }
                complete {
                  "OK"
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ post {

        path("approveDeclineRequest") {
          entity(as[approveDeclineRequestJson]) { (approveDeclineRequestJson) =>
            // parameters("userId".as[Int], "friendId".as[Int], "decision".as[Boolean]) { (userId, friendId, decision) =>
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                implicit val timeout = Timeout(5 seconds)
                var future: Future[ObjectForFriend] = ask(worker, approveDeclineRequest(approveDeclineRequestJson.userId, approveDeclineRequestJson.friendId, approveDeclineRequestJson.decision, friendList, friendRequestsList, userList)).mapTo[ObjectForFriend]
                future onSuccess {
                  case result => {
                    friendRequestsList = result.friendRequestsList
                    friendList = result.friendList
                    userList = result.userList
                  }
                }
                complete {
                  "OK"
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("user" / IntNumber / "feed") { (userId) =>
          get {
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                userPostList.get(userId) match {
                  case Some(userRoute) => complete(userRoute)
                  case None            => complete(NotFound -> s"No posts for user id $userId was found!")
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("getPublicKey" / IntNumber) { (userId) =>
          get {
             headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                 userList.get(userId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No EncPosts for user id $userId was found!")
            }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }
           
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("user" / IntNumber / "encryptedFeed") { (userId) =>
          get {
             headerValueByName("token") { token =>
              if (tokens.contains(token)) {
             userEncryptedPostList.get(userId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No EncPosts for user id $userId was found!")
            }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }
            
          }
        }
      } ~ post {
        path("userPost") {
          entity(as[userPostJson]) { (userPostJson) =>
            //parameters("userId".as[Int], "fromUser".as[Int], "post".as[String]) { (userId, fromUser, post) =>
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                implicit val timeout = Timeout(5 seconds)
                var future: Future[scala.collection.mutable.Map[Int, List[UserPost]]] = ask(worker, userPostMethod(userPostJson.userId, userPostJson.fromUser, userPostJson.postId, userPostJson.post, friendList, userPostList)).mapTo[scala.collection.mutable.Map[Int, List[UserPost]]]
                future onSuccess {
                  case result => {
                    userPostCounter = userPostCounter + 1
                    userPostList = result
                  }
                }
                complete {
                  "OK"
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ post {
        path("userEncryptedPost") {
          entity(as[userEncryptedPostJson]) { (userEncryptedPostJson) =>
             headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                implicit val timeout = Timeout(5 seconds)
            var future: Future[scala.collection.mutable.Map[Int, List[UserEncryptedPost]]] = ask(worker, userEncryptedPostMethod(userEncryptedPostJson.userId, userEncryptedPostJson.fromUser, userEncryptedPostJson.postId, userEncryptedPostJson.post, userEncryptedPostJson.key, friendList, userEncryptedPostList)).mapTo[scala.collection.mutable.Map[Int, List[UserEncryptedPost]]]
            future onSuccess {
              case result => {
                //userPostCounter = userPostCounter + 1
                userEncryptedPostList = result
              }
            }
            complete {
              "OK"
            }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }
            

          }
        }
      } ~ post {
        path("deletePost") {
          entity(as[deleteUserPostJson]) { (deleteUserPostJson) =>
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                implicit val timeout = Timeout(5 seconds)
                var future: Future[scala.collection.mutable.Map[Int, List[UserPost]]] = ask(worker, deleteUserPost(deleteUserPostJson.userId, deleteUserPostJson.fromUser, deleteUserPostJson.postId, userPostList)).mapTo[scala.collection.mutable.Map[Int, List[UserPost]]]
                future onSuccess {
                  case result => {
                    userPostList = result
                  }
                }
                complete {
                  "OK"
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }
            //parameters("userId".as[Int], "fromUser".as[Int], "postId".as[Int]) { (userId, fromUser, postId) =>
          }
        }
      } ~ post {
        path("userAlbum") {
          entity(as[userImageJson]) { (pictureJson) =>
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                 implicit val timeout = Timeout(5 seconds)
                var future: Future[scala.collection.mutable.Map[Int, List[userImageJson]]] = ask(worker, setUserPicture(pictureJson, postUserPictureList)).mapTo[scala.collection.mutable.Map[Int, List[userImageJson]]]
                future onSuccess {
                  case result => {
                    picturePostCount = picturePostCount + 1
                    postUserPictureList = result
                    //println(postUserPictureList)
                  }
                }
                complete {
                  "OK"
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }
               
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("user" / IntNumber / "album") { (userId) =>
          get {
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                postUserPictureList.get(userId) match {
                  case Some(userRoute) => complete(userRoute)
                  case None            => complete(NotFound -> s"No pictures for user id $userId was found!")
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("user" / IntNumber / "picture" / IntNumber) { (userId, pictureId) =>
          get {
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                getUserPictureIndex(userId, pictureId) match {
                  case Some(userRoute) => complete(userRoute)
                  case None            => complete(NotFound -> s"No pictures for user id $userId was found!")
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ post {
        path("pageAlbum") {
          entity(as[pageImageJson]) { (pictureJson) =>
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                implicit val timeout = Timeout(5 seconds)
                var future: Future[scala.collection.mutable.Map[Int, List[pageImageJson]]] = ask(worker, setPagePicture(pictureJson, postPagePictureList)).mapTo[scala.collection.mutable.Map[Int, List[pageImageJson]]]
                future onSuccess {
                  case result => {
                    picturePostCount = picturePostCount + 1
                    postPagePictureList = result
                  }
                }
                complete {
                  "OK"
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ post {
        path("logout") {
            headerValueByName("token") { token =>
              tokens -= token
               complete {
                  "You have been logged out"
                }
                }
               
             } 
      }~ respondWithMediaType(MediaTypes.`application/json`) {
        path("page" / IntNumber / "album") { (pageId) =>
          get {
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                postPagePictureList.get(pageId) match {
                  case Some(userRoute) => complete(userRoute)
                  case None            => complete(NotFound -> s"No pictures for page id $pageId was found!")
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("page" / IntNumber / "picture" / IntNumber) { (pageId, pictureId) =>
          get {
            headerValueByName("token") { token =>
              if (tokens.contains(token)) {
                getPagePictureIndex(pageId, pictureId) match {
                  case Some(userRoute) => complete(userRoute)
                  case None            => complete(NotFound -> s"No pictures for page id $pageId was found!")
                }
              } else {
                println("NOT LOGGED IN!!!!!")
                complete(NotFound -> s"You are not logged in!!!")
              }
            }

          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("Statistics") {
          get {
            complete(statistics(pagePostCounter, userPostCounter, pagePostCounter + userPostCounter, picturePostCount, friendRequestSent))
          }
        }
      }

  }

  def getUserPictureIndex(userId: Int, pictureId: Int): Option[userImageJson] = {
    if (postUserPictureList.contains(userId)) {
      var tempPostList: List[userImageJson] = postUserPictureList(userId)
      var i = 0
      for (i <- 0 to tempPostList.size - 1) {
        if (tempPostList(i).pictureId.toInt == pictureId) {
          return Some(tempPostList(i))
        }
      }

    }

    return None
  }
  def getPagePictureIndex(pageId: Int, pictureId: Int): Option[pageImageJson] = {
    if (postPagePictureList.contains(pageId)) {
      var tempPostList: List[pageImageJson] = postPagePictureList(pageId)
      var i = 0
      for (i <- 0 to tempPostList.size - 1) {
        if (tempPostList(i).pictureId.toInt == pictureId) {
          return Some(tempPostList(i))
        }
      }
    }

    return None
  }

  def receive = {

    runRoute(routes)

  }
  
  def encryptRSA(randomToken: String, key: PublicKey): String = {
  
   var cipher = Cipher.getInstance("RSA");
            // ENCRYPT using the PUBLIC key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            var encryptedBytes = cipher.doFinal(randomToken.getBytes("ISO-8859-1"));
   
    return Base64.encodeBase64String(encryptedBytes)
  }
}

class ServerWorker extends Actor {
  implicit def executionContext = context.dispatcher

  def receive = {

    //*********************************************************************************************************************
    case updatePageLikeList(pageId: Int, userId: Int, pageList: scala.collection.mutable.Map[Int, Page], pageLikeList: scala.collection.mutable.Map[Int, List[Int]]) => {
      //println("like"+self)
      if (!pageLikeList.contains(pageId)) {
        pageLikeList += pageId -> List(userId)
        // println(pageList.size)
        pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes + 1, pageList(pageId).pagePublicKey)
        //pageLikeList foreach {case (key, value) => println (key + "---->" + value.toList)}
      } else {
        if (!pageLikeList(pageId).contains(userId)) {
          pageLikeList(pageId) ::= userId
          pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes + 1, pageList(pageId).pagePublicKey)
        }

      }
      sender ! ObjectForLike(pageList, pageLikeList)
    }

    case updateUnlike(pageId: Int, userId: Int, pageList: scala.collection.mutable.Map[Int, Page], pageLikeList: scala.collection.mutable.Map[Int, List[Int]]) => {
      if (pageLikeList.contains(pageId) && pageLikeList(pageId).contains(userId)) {
        var index = pageLikeList(pageId).indexOf(userId)
        pageLikeList(pageId) = pageLikeList(pageId).take(index) ++ pageLikeList(pageId).drop(index + 1)
        pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes - 1, pageList(pageId).pagePublicKey)
        //pageLikeList foreach {case (key, value) => println (key + "-->" + value.toList)}
      }
      sender ! ObjectForLike(pageList, pageLikeList)
    }

    case pagePost(pageId: Int, postId: Int, post: String, pagePostList: scala.collection.mutable.Map[Int, List[Post]]) => {

      if (!pagePostList.contains(pageId)) {
        pagePostList += pageId -> List(Post(postId, pageId, post))
      } else {
        pagePostList(pageId) ::= Post(postId, pageId, post)

      }
      sender ! pagePostList
    }
    case deletePagePost(pageId: Int, postId: Int, pagePostList: scala.collection.mutable.Map[Int, List[Post]]) => {
      if (pagePostList.contains(pageId)) {
        var tempPostList: List[Post] = pagePostList(pageId)
        var i = 0
        for (i <- 0 to tempPostList.size - 1) {
          if (i < (tempPostList.size - 1) && tempPostList(i).postId == postId) {
            tempPostList = tempPostList.take(i) ++ tempPostList.drop(i + 1)
          }
        }
        pagePostList(pageId) = tempPostList
      }
      sender ! pagePostList

    }

    case userPostMethod(userId: Int, fromUser: Int, postId: Int, post: String, friendList: scala.collection.mutable.Map[Int, List[User]], userPostList: scala.collection.mutable.Map[Int, List[UserPost]]) => {

      var tempFriendList: List[User] = List()
      var isFriend = false
      if (!friendList.isEmpty && userId != fromUser) {
        tempFriendList = friendList(userId)
        //Friendship Check
        for (i <- 0 to tempFriendList.size - 1) {
          if (tempFriendList(i).userId == fromUser)
            isFriend = true
        }
      }

      if (userId == fromUser || isFriend) {
        if (!userPostList.contains(userId)) {
          userPostList += userId -> List(UserPost(postId, fromUser, post))
        } else {
          userPostList(userId) ::= UserPost(postId, fromUser, post)

        }
      }
      sender ! userPostList

    }
    case userEncryptedPostMethod(userId: Int, fromUser: Int, postId: Int, post: String, key: String, friendList: scala.collection.mutable.Map[Int, List[User]], userEncryptedPostList: scala.collection.mutable.Map[Int, List[UserEncryptedPost]]) => {
       //println("Inside userEncryptedPostMethod")
      var tempFriendList: List[User] = List()
      if (!userEncryptedPostList.contains(userId)) {
        userEncryptedPostList += userId -> List(UserEncryptedPost(postId, fromUser, post, key))
      } else {
        userEncryptedPostList(userId) ::= UserEncryptedPost(postId, fromUser, post, key)

      }
      //println(userEncryptedPostList)
      sender ! userEncryptedPostList

    }

    case deleteUserPost(userId: Int, fromUser: Int, postId: Int, userPostList: scala.collection.mutable.Map[Int, List[UserPost]]) => {
      if (userPostList.contains(userId)) {

        var tempPostList: List[UserPost] = userPostList(userId)
        for (i <- 0 to tempPostList.size - 1) {
          if (i < (tempPostList.size - 1) && tempPostList(i).postId == postId && (tempPostList(i).admin_creator == userId || tempPostList(i).admin_creator == fromUser)) {
            tempPostList = tempPostList.take(i) ++ tempPostList.drop(i + 1)
          }
        }
        userPostList(userId) = tempPostList
      }
      sender ! userPostList
    }
    case setUserPicture(imageJson: userImageJson, postUserPictureList: scala.collection.mutable.Map[Int, List[userImageJson]]) => {
      if (!postUserPictureList.contains(imageJson.userId.toInt)) {
        postUserPictureList += imageJson.userId.toInt -> List(imageJson)

      } else {
        postUserPictureList(imageJson.userId.toInt) ::= imageJson
      }
      sender ! postUserPictureList
    }

    case setPagePicture(imageJson: pageImageJson, postPagePictureList: scala.collection.mutable.Map[Int, List[pageImageJson]]) => {
      if (!postPagePictureList.contains(imageJson.pageId.toInt)) {
        postPagePictureList += imageJson.pageId.toInt -> List(imageJson)

      } else {
        postPagePictureList(imageJson.pageId.toInt) ::= imageJson
      }
      sender ! postPagePictureList
    }

    case friendRequest(userId: Int, friendId: Int, friendRequestsList: scala.collection.mutable.Map[Int, List[User]], userList: scala.collection.mutable.Map[Int, User]) => {
      if (!friendRequestsList.contains(userId)) {
        friendRequestsList += userId -> List(userList(friendId))
      } else {
        if (!friendRequestsList(userId).contains(userList(friendId))) {
          friendRequestsList(userId) ::= userList(friendId)
        }
      }

      sender ! friendRequestsList
    }

    case approveDeclineRequest(userId: Int, friendId: Int, decision: Boolean, friendList: scala.collection.mutable.Map[Int, List[User]], friendRequestsList: scala.collection.mutable.Map[Int, List[User]], userList: scala.collection.mutable.Map[Int, User]) => {
      if (!friendRequestsList.isEmpty && friendRequestsList.contains(userId)) {

        var tempRequestsList: List[User] = friendRequestsList(userId)
        for (i <- 0 to tempRequestsList.size - 1) {
          if (i < (tempRequestsList.size - 1) && tempRequestsList(i).userId == friendId) {
            if (decision) {
              if (!friendList.contains(userId)) {
                friendList += userId -> List(userList(friendId))

              } else {
                friendList(userId) ::= userList(friendId)

              }

              if (!friendList.contains(friendId)) {
                friendList += friendId -> List(userList(userId))
              } else {
                friendList(friendId) ::= userList(userId)
              }

            } else {

            }

            tempRequestsList = tempRequestsList.take(i) ++ tempRequestsList.drop(i + 1)
            friendRequestsList(userId) = tempRequestsList

          }
        }
      }

      sender ! ObjectForFriend(friendList, friendRequestsList, userList)
    }
  }

}

