
import akka.actor.Actor
import spray.routing.HttpService
import spray.http.MediaTypes
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpServiceActor
import spray.http.StatusCodes._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import spray.json.DefaultJsonProtocol

case class User(userId: Int, name: String, gender: String)
case class Page(pageId: Int, pageName: String, likes: Int)
case class PagePost(pageId: Int, posts: List[String])
case class Post(postId:Int, admin_creator:Int, post:String)

object User extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat3(User.apply)
}
object Page extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat3(Page.apply)
}
object PagePost extends DefaultJsonProtocol {
  implicit var pagePostFormat = jsonFormat2(PagePost.apply)
}
object Post extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat3(Post.apply)
}

class ServerActor extends HttpServiceActor {
  override def actorRefFactory = context

  val userRoute = new UserRoute {
    override implicit def actorRefFactory = context
  }

  def receive = runRoute(userRoute.routes)
}

trait UserRoute extends HttpService {
  implicit def executionContext = actorRefFactory.dispatcher

  var userList = scala.collection.mutable.Map[Int, User]()
  var pageList = scala.collection.mutable.Map[Int, Page]()
  var pageLikeList = scala.collection.mutable.Map[Int, List[Int]]()
 // var pagePostList1 = scala.collection.mutable.Map[Int, PagePost]()
  var pagePostList = scala.collection.mutable.Map[Int, List[Post]]()
   var postIdCreator = 1;

  val routes = {
    respondWithMediaType(MediaTypes.`application/json`) {
      path("user" / IntNumber) { (userId) =>
        get {
          userList.get(userId) match {
            case Some(userRoute) => complete(userRoute)
            case None            => complete(NotFound -> s"No user with id $userId was found!")
          }
        }
      }
    } ~
      post {
        path("registerUser") {
          parameters("userId".as[Int], "name".as[String], "gender".as[String]) { (userId, name, gender) =>
            userList += userId -> User(userId, name, gender)
            complete {
              "User Created - " + name
            }
          }
        }
      } ~ post {
        path("registerPage") {
          parameters("pageId".as[Int], "pageName".as[String]) { (pageId, pageName) =>
            pageList += pageId -> Page(pageId, pageName, 0)
            complete {
              "Page Created - " + pageName
            }
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("page" / IntNumber) { (pageId) =>
          get {
            pageList.get(pageId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No page with id $pageId was found!")
            }
          }
        }
      } ~ post {
        path("likePage") {
          parameters("pageId".as[Int], "userId".as[Int]) { (pageId, userId) =>
            updatePageLikeList(pageId, userId)
            complete {
              "OK"
            }
          }
        }
      }~ post {
        path("unlikePage") {
          parameters("pageId".as[Int], "userId".as[Int]) { (pageId, userId) =>
            updateUnlike(pageId, userId)
            complete {
              "OK"
            }
          }
        }
      }~ post {
        path("pagePost") {
          parameters("pageId".as[Int], "post".as[String]) { (pageId,post) =>
            pagePost(pageId,post)
            complete {
              "OK"
            }
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("page" / IntNumber /"feed") { (pageId) =>
          get {
            pagePostList.get(pageId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No posts for page id $pageId was found!")
            }
          }
        }
      }~ post {
        path("deletePost") {
          parameters("pageId".as[Int], "postId".as[Int]) { (pageId,postId) =>
            deletePagePost(pageId,postId)
            complete {
              "OK"
            }
          }
        }
      } 

  }

//  def incrementLikeCount(pageId: Int, userId: Int) = {
//    pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes + 1)
//  }

  def updatePageLikeList(pageId: Int, userId: Int) = {
    if (!pageLikeList.contains(pageId)) {
      pageLikeList += pageId -> List(userId)
      pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes + 1)
      //pageLikeList foreach {case (key, value) => println (key + "---->" + value.toList)}
    } else {
      if (!pageLikeList(pageId).contains(userId)){
        pageLikeList(pageId) ::= userId
        pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes + 1)  
      }  
      //pageLikeList foreach {case (key, value) => println (key + "-->" + value.toList)}
    }

  }
  def updateUnlike(pageId: Int, userId: Int) = {
    if (pageLikeList.contains(pageId) && pageLikeList(pageId).contains(userId)) {
    var index = pageLikeList(pageId).indexOf(userId)
     pageLikeList(pageId) = pageLikeList(pageId).take(index) ++ pageLikeList(pageId).drop(index+1)
     pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes - 1)
     //pageLikeList foreach {case (key, value) => println (key + "-->" + value.toList)}
    }

  }
  
//  def pagePost1(pageId:Int, post:String) = {
//    if (!pagePostList.contains(pageId)) {
//      pagePostList += pageId -> PagePost(pageId, List(post))
//    }else{
//     var tempPostList:List[String] = pagePostList(pageId).posts
//     tempPostList ::= post
//     pagePostList(pageId) = PagePost(pageId, tempPostList)
//      //pagePostList(pageId) ::= post
//    }
//    
//  }
  
  
  def pagePost(pageId:Int, post:String) = {
   
    if (!pagePostList.contains(pageId)) {
      pagePostList += pageId -> List(Post(postIdCreator,pageId,post))
    }else{
     // println(pagePostList(pageId).toList)
     postIdCreator = postIdCreator+1
     pagePostList(pageId) ::= Post(postIdCreator,pageId,post)
    // println(pagePostList(pageId).toList)
     
    }
    
  }
  
  def deletePagePost(pageId:Int, postId:Int) = {
    if (pagePostList.contains(pageId)) {
      var tempPostList:List[Post] = pagePostList(pageId)
      var i=0
      for( i <- 0 to tempPostList.size-1){
        if(tempPostList(i).postId==postId){
          tempPostList = tempPostList.take(i) ++ tempPostList.drop(i+1)
        }
      }
      pagePostList(pageId) = tempPostList
    }
    
  }
  

}

