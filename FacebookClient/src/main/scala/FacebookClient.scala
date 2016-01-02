import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import akka.actor._
import akka.actor.Actor
import akka.actor.ActorDSL._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter
import spray.http._
import spray.client.pipelining._
import spray.routing._
import spray.http.MediaTypes
import spray.routing.Directive.pimpApply
import spray.routing.SimpleRoutingApp
import spray.routing.directives.ParamDefMagnet.apply
import scala.concurrent.Future
import scala.util.{ Success, Failure }
import spray.client.pipelining.{ Get, sendReceive }
import spray.client.pipelining.{ Post, sendReceive }
import java.io._
import spray.http.{ MediaTypes, BodyPart, MultipartFormData }
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import java.util.concurrent.TimeUnit;
import akka.util
import java.nio.file.{ Files, Paths }
import org.apache.commons.codec.binary
import org.apache.commons.codec.binary.Base64
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.json.JSONArray
import org.json.JSONObject;
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import javax.crypto.Cipher;
import org.apache.commons.lang.SerializationUtils

//import scala.concurrent.ExecutionContext.Implicits.global

import spray.httpx._

import spray.httpx.SprayJsonSupport
import spray.json.AdditionalFormats

case class LogoutUser(userId: Int)
case class LoginUser(userId: Int)
case class LoginPage(pageId: Int)
case class RegisterUser(userId: Int)
case class GetUser(userId: Int)
case class RegisterPage(pageId: Int)
case class GetPage(pageId: Int)
case class likePage(pageId: Int, userId: Int)
case class pagePost(pageId: Int)
case class userPost(userId: Int, fromUser: Int)
case class userEncryptedPost(userId: Int, fromUser: Int)
case class unlikePage(pageId: Int, userId: Int)
case class getPageFeed(pageId: Int)
case class getUserFeed(userId: Int)
case class getEncryptedUserFeed(userId: Int)
case class deletePagePost(pageId: Int, postId: Int)
case class deleteUserPost(userId: Int, fromUser: Int, postId: Int)
case class getFriendList(userId: Int)
case class getFriendRequestList(userId: Int)
case class sendFriendRequest(userId: Int, friendId: Int)
case class postUserPicture(userId: Int, pictureId: Int)
case class getUserPicture(userId: Int, pictureId: Int)
case class postPagePicture(pageId: Int, pictureId: Int)
case class getPagePicture(pageId: Int, pictureId: Int)
case class approveDeclineRequest(userId: Int, friendId: Int, decision: Boolean)
case class initialNetwork(numUsers: Int)
case class simulateVisitPage(actorType: String)
case class simulatePostPage(actorType: String)
case class simulateLikePage(actorType: String)
case class simulateUnlikePage(actorType: String)
case class simulateReadPageFeed(actorType: String)
case class simulateDeletePagePost(actorType: String)
case class simulatedeleteUserPost(actorType: String)
case class simulateFriendRequest(actorType: String)
case class simulateStatusUpdate(actorType: String)
case class simulateReadUserFeed(actorType: String)
case class simulateVisitUserProfile(actorType: String)
case class simulateapproveDeclineRequest(actorType: String)
case class simulategetFriendList(actorType: String)
case class simulategetFriendRequestList(actorType: String)
case class simulatePostPicture(actorType: String)
case class simulatepostUserPicture(actorType: String)
case class simulatepostPagePicture(actorType: String)
case class simulategettUserPicture(actorType: String)
case class simulategetPagePicture(actorType: String)
case class statistics()
case class simulationPostPictureandfriendRequest(index: Int, actorType: String)
case class stop()
case class getUserProfileKey(userPublicKey: PublicKey)
case class getPageProfileKey(paegPublicKey: PublicKey)
case class getPagePostKeys(pagePublicKey: PublicKey)
case class getUserPostKeys(userId: Int, userPublicKey: PublicKey)
case class getPagePictureKey(pictureId: Int, pagePublicKey: PublicKey)
case class getUserPictureKey(userId: Int, pictureId: Int, pagePublicKey: PublicKey)
case class updateFriendList(userId: Int)
case class setOtherActorRef(multiMediaSavyActors: ArrayBuffer[ActorRef], lowEngagedActors: ArrayBuffer[ActorRef], textSavyActors: ArrayBuffer[ActorRef], highEngagedActors: ArrayBuffer[ActorRef], multiMediaSpecialistActors: ArrayBuffer[ActorRef])

object FacebookClient extends App {

  override def main(args: Array[String]) {
    var numUser: Int = 0
    if (args.length == 0 || args.length != 1) {
      println("Wrong Arguments");
    } else {
      numUser = args(0).toInt
    }
    val system = ActorSystem("FacebookClientSystem")
    val master: ActorRef = system.actorOf(Props(new networkSimulator(system)), name = "Master")

    master ! initialNetwork(numUser)

  }

}

class networkSimulator(system: ActorSystem) extends Actor {
  val multiMediaSavyUsers = new ArrayBuffer[ActorRef]()
  val lowEngagedUsers = new ArrayBuffer[ActorRef]()
  val textSavyPages = new ArrayBuffer[ActorRef]()
  val highEngagedUsers = new ArrayBuffer[ActorRef]()
  val multiMediaSpecialistPages = new ArrayBuffer[ActorRef]()
  var visitPageIntializer: Cancellable = null
  var numActors = 0
  var numPages = 0
  var numUsers = 0
  var numMultiMediaSavyUsers = 0
  var numLowEngagedUsers = 0
  var numTextSavyPages = 0
  var numHighEngagedUsers = 0
  var numMultiMediaSpecialistPages = 0

  import context.dispatcher
  def receive = {

    case initialNetwork(numberOfActors: Int) => {
      numActors = numberOfActors
      numPages = (numActors * 0.205).toInt
      numUsers = (numActors * 0.795).toInt
      numMultiMediaSavyUsers = (numActors * 0.17).toInt
      numLowEngagedUsers = (numActors * 0.225).toInt
      numTextSavyPages = (numActors * 0.18).toInt
      numHighEngagedUsers = (numActors * 0.4).toInt
      numMultiMediaSpecialistPages = (numActors * 0.025).toInt

      //props all actors

      for (i <- 0 until numMultiMediaSavyUsers) {
        multiMediaSavyUsers += system.actorOf(Props(new Client(system)), name = "multiMediaSavyUsers" + i)
      }
      for (i <- 0 until numLowEngagedUsers) {
        lowEngagedUsers += system.actorOf(Props(new Client(system)), name = "lowEngagedUsers" + i)
      }
      for (i <- 0 until numTextSavyPages) {
        textSavyPages += system.actorOf(Props(new Client(system)), name = "textSavyPages" + i)
      }
      for (i <- 0 until numHighEngagedUsers) {
        highEngagedUsers += system.actorOf(Props(new Client(system)), name = "highEngagedUsers" + i)
      }
      for (i <- 0 until numMultiMediaSpecialistPages) {
        multiMediaSpecialistPages += system.actorOf(Props(new Client(system)), name = "multiMediaSpecialistPages" + i)
      }

      for (i <- 0 until numMultiMediaSavyUsers) {
        multiMediaSavyUsers(i) ! RegisterUser(i)
        multiMediaSavyUsers(i) ! setOtherActorRef(multiMediaSavyUsers, lowEngagedUsers, textSavyPages, highEngagedUsers, multiMediaSpecialistPages)
      }
      for (i <- 0 until numLowEngagedUsers) {
        lowEngagedUsers(i) ! RegisterUser(i + numMultiMediaSavyUsers)
        lowEngagedUsers(i) ! setOtherActorRef(multiMediaSavyUsers, lowEngagedUsers, textSavyPages, highEngagedUsers, multiMediaSpecialistPages)
      }
      for (i <- 0 until numTextSavyPages) {
        textSavyPages(i) ! RegisterPage(i)
        textSavyPages(i) ! setOtherActorRef(multiMediaSavyUsers, lowEngagedUsers, textSavyPages, highEngagedUsers, multiMediaSpecialistPages)
      }
      for (i <- 0 until numHighEngagedUsers) {
        highEngagedUsers(i) ! RegisterUser(i + numLowEngagedUsers + numMultiMediaSavyUsers)
        highEngagedUsers(i) ! setOtherActorRef(multiMediaSavyUsers, lowEngagedUsers, textSavyPages, highEngagedUsers, multiMediaSpecialistPages)
      }
      for (i <- 0 until (numMultiMediaSpecialistPages)) {
        multiMediaSpecialistPages(i) ! RegisterPage(i + numTextSavyPages)
        multiMediaSpecialistPages(i) ! setOtherActorRef(multiMediaSavyUsers, lowEngagedUsers, textSavyPages, highEngagedUsers, multiMediaSpecialistPages)
      }

      for (i <- 0 until numMultiMediaSavyUsers) {
        context.system.scheduler.scheduleOnce(FiniteDuration(45000, TimeUnit.MILLISECONDS), multiMediaSavyUsers(i), LoginUser(i))
        //multiMediaSavyUsers(i) ! LoginUser(i)

      }
      for (i <- 0 until numLowEngagedUsers) {
        context.system.scheduler.scheduleOnce(FiniteDuration(45000, TimeUnit.MILLISECONDS), lowEngagedUsers(i), LoginUser(i + numMultiMediaSavyUsers))
        //lowEngagedUsers(i) ! LoginUser(i + numMultiMediaSavyUsers)

      }
      for (i <- 0 until numTextSavyPages) {
        context.system.scheduler.scheduleOnce(FiniteDuration(45000, TimeUnit.MILLISECONDS), textSavyPages(i), LoginPage(i))
        //textSavyPages(i) ! LoginPage(i)

      }
      for (i <- 0 until numHighEngagedUsers) {
        context.system.scheduler.scheduleOnce(FiniteDuration(45000, TimeUnit.MILLISECONDS), highEngagedUsers(i), LoginUser(i + numLowEngagedUsers + numMultiMediaSavyUsers))
        //highEngagedUsers(i) ! LoginUser(i + numLowEngagedUsers + numMultiMediaSavyUsers)

      }
      for (i <- 0 until (numMultiMediaSpecialistPages)) {
        context.system.scheduler.scheduleOnce(FiniteDuration(45000, TimeUnit.MILLISECONDS), multiMediaSpecialistPages(i), LoginPage(i + numTextSavyPages))
        //multiMediaSpecialistPages(i) ! LoginPage(i + numTextSavyPages)

      }
      println("Simulation Started...")

      //Post text,status,picture and send friend requests
      for (i <- 0 until numMultiMediaSavyUsers) {
        context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(1000, TimeUnit.MILLISECONDS), self, simulationPostPictureandfriendRequest(i, "C1"))

      }
      //      for (i <- 0 until numLowEngagedUsers) {
      //        context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(4000, TimeUnit.MILLISECONDS), self, simulationPostPictureandfriendRequest(i, "C2"))
      //
      //      }
      //      for (i <- 0 until numTextSavyPages) {
      //        context.system.scheduler.schedule(FiniteDuration(45000, TimeUnit.MILLISECONDS), FiniteDuration(2000, TimeUnit.MILLISECONDS), self, simulationPostPictureandfriendRequest(i, "C3"))
      //
      //      }
      //      for (i <- 0 until numHighEngagedUsers) {
      //        context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(1100, TimeUnit.MILLISECONDS), self, simulationPostPictureandfriendRequest(i, "C4"))
      //
      //      }
      //      for (i <- 0 until (numMultiMediaSpecialistPages)) {
      //        context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(8800, TimeUnit.MILLISECONDS), self, simulationPostPictureandfriendRequest(i, "C5"))
      //
      //      }

      // Read Page feeds
      //context.system.scheduler.schedule(FiniteDuration(30000, TimeUnit.MILLISECONDS), FiniteDuration(800, TimeUnit.MILLISECONDS), self, simulateReadPageFeed("C1"))
      //context.system.scheduler.schedule(FiniteDuration(30000, TimeUnit.MILLISECONDS), FiniteDuration(500, TimeUnit.MILLISECONDS), self, simulateReadPageFeed("C2"))
      //context.system.scheduler.schedule(FiniteDuration(30000, TimeUnit.MILLISECONDS), FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateReadPageFeed("C4"))

      //Read User feeds
      context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(800, TimeUnit.MILLISECONDS), self, simulateReadUserFeed("C1"))
      //context.system.scheduler.schedule(FiniteDuration(45000, TimeUnit.MILLISECONDS), FiniteDuration(500, TimeUnit.MILLISECONDS), self, simulateReadUserFeed("C2"))
      //context.system.scheduler.schedule(FiniteDuration(45000, TimeUnit.MILLISECONDS), FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateReadUserFeed("C4"))

      //Like Pages
      context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(500, TimeUnit.MILLISECONDS), self, simulateLikePage("C1"))
      context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(2000, TimeUnit.MILLISECONDS), self, simulateLikePage("C2"))
      context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(500, TimeUnit.MILLISECONDS), self, simulateLikePage("C4"))

      //UnLike Pages
//      context.system.scheduler.schedule(FiniteDuration(65000, TimeUnit.MILLISECONDS), FiniteDuration(5000, TimeUnit.MILLISECONDS), self, simulateUnlikePage("C1"))
//      context.system.scheduler.schedule(FiniteDuration(65000, TimeUnit.MILLISECONDS), FiniteDuration(5000, TimeUnit.MILLISECONDS), self, simulateUnlikePage("C4"))

      //Delete Posts
      //context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(15000, TimeUnit.MILLISECONDS), self, simulatedeleteUserPost("C1"))
      //context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(10000, TimeUnit.MILLISECONDS), self, simulateDeletePagePost("C3"))
      //context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(15000, TimeUnit.MILLISECONDS), self, simulatedeleteUserPost("C4"))
      //context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(10000, TimeUnit.MILLISECONDS), self, simulateDeletePagePost("C5"))

      //ApprovefriendRequests
      //context.system.scheduler.schedule(FiniteDuration(30000, TimeUnit.MILLISECONDS), FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateapproveDeclineRequest("C1"))
      //context.system.scheduler.schedule(FiniteDuration(30000, TimeUnit.MILLISECONDS), FiniteDuration(500, TimeUnit.MILLISECONDS), self, simulateapproveDeclineRequest("C2"))
      //context.system.scheduler.schedule(FiniteDuration(30000, TimeUnit.MILLISECONDS), FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateapproveDeclineRequest("C4"))

      // Visit Pages
      //context.system.scheduler.schedule(FiniteDuration(30000, TimeUnit.MILLISECONDS), FiniteDuration(800, TimeUnit.MILLISECONDS), self, simulateVisitPage("C1"))
      //context.system.scheduler.schedule(FiniteDuration(30000, TimeUnit.MILLISECONDS), FiniteDuration(1000, TimeUnit.MILLISECONDS), self, simulateVisitPage("C2"))
      //context.system.scheduler.schedule(FiniteDuration(30000, TimeUnit.MILLISECONDS), FiniteDuration(200, TimeUnit.MILLISECONDS), self, simulateVisitPage("C4"))

      //Read pictures
      context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(1000, TimeUnit.MILLISECONDS), self, simulategettUserPicture("C1"))
      //context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(2000, TimeUnit.MILLISECONDS), self, simulategettUserPicture("C2"))
      //context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(2000, TimeUnit.MILLISECONDS), self, simulategettUserPicture("C4"))
      //context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(1000, TimeUnit.MILLISECONDS), self, simulategetPagePicture("C1"))
      //context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(2000, TimeUnit.MILLISECONDS), self, simulategetPagePicture("C2"))
      //context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(2000, TimeUnit.MILLISECONDS), self, simulategetPagePicture("C4"))

      //context.system.scheduler.scheduleOnce(FiniteDuration(60000, TimeUnit.MILLISECONDS), multiMediaSavyUsers(0), statistics())
      //context.system.scheduler.schedule(FiniteDuration(30000, TimeUnit.MILLISECONDS), FiniteDuration(800, TimeUnit.MILLISECONDS), self, simulateVisitUserProfile("C1"))
      //context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(500, TimeUnit.MILLISECONDS), multiMediaSavyUsers(0), getPageFeed(121))

      //Page Picture demo 
      //context.system.scheduler.scheduleOnce(FiniteDuration(60000, TimeUnit.MILLISECONDS), textSavyPages(0), postPagePicture(0, 0))
      //context.system.scheduler.scheduleOnce(FiniteDuration(62000, TimeUnit.MILLISECONDS), textSavyPages(0), getPagePicture(0, 0))
      //User Picture demo
      //context.system.scheduler.scheduleOnce(FiniteDuration(60000, TimeUnit.MILLISECONDS), multiMediaSavyUsers(0), postUserPicture(0, 0))
      //context.system.scheduler.scheduleOnce(FiniteDuration(62000, TimeUnit.MILLISECONDS), multiMediaSavyUsers(0), getUserPicture(0, 0))
      //context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(2000, TimeUnit.MILLISECONDS), multiMediaSavyUsers(0), userEncryptedPost(1, 0))
      //context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(3000, TimeUnit.MILLISECONDS), multiMediaSavyUsers(1), getEncryptedUserFeed(1))

      //context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(2000, TimeUnit.MILLISECONDS), multiMediaSavyUsers(0), postUserPicture(1, 1))
      //context.system.scheduler.schedule(FiniteDuration(60000, TimeUnit.MILLISECONDS), FiniteDuration(3000, TimeUnit.MILLISECONDS), multiMediaSavyUsers(1), getUserPicture(1,1))
      
      context.system.scheduler.scheduleOnce(FiniteDuration(70000, TimeUnit.MILLISECONDS), multiMediaSavyUsers(0), LogoutUser(0))
      context.system.scheduler.scheduleOnce(FiniteDuration(72000, TimeUnit.MILLISECONDS), multiMediaSavyUsers(0), getEncryptedUserFeed(0))

    }

    case simulateVisitPage(actorType: String) => {

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(Random.nextInt(numMultiMediaSavyUsers)) ! GetPage(Random.nextInt(numPages))
        case "C2" => lowEngagedUsers(Random.nextInt(numLowEngagedUsers)) ! GetPage(Random.nextInt(numPages))
        case "C3" => textSavyPages(Random.nextInt(numTextSavyPages)) ! GetPage(Random.nextInt(numPages))
        case "C4" => highEngagedUsers(Random.nextInt(numHighEngagedUsers)) ! GetPage(Random.nextInt(numPages))
        case "C5" => multiMediaSpecialistPages(Random.nextInt(numMultiMediaSpecialistPages)) ! GetPage(Random.nextInt(numPages))
      }

    }

    case simulateVisitUserProfile(actorType: String) => {
      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(Random.nextInt(numMultiMediaSavyUsers)) ! GetUser(Random.nextInt(numUsers))
        case "C2" => lowEngagedUsers(Random.nextInt(numLowEngagedUsers)) ! GetUser(Random.nextInt(numUsers))
        case "C4" => highEngagedUsers(Random.nextInt(numHighEngagedUsers)) ! GetUser(Random.nextInt(numUsers))
      }

    }

    case simulateLikePage(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! likePage(Random.nextInt(numPages), numMultiMediaSavyUserIndex)
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! likePage(Random.nextInt(numPages), numLowEngagedUserIndex)
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! likePage(Random.nextInt(numPages), numHighEngagedUserIndex)
      }

    }

    case simulateUnlikePage(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! unlikePage(Random.nextInt(numPages), numMultiMediaSavyUserIndex)
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! unlikePage(Random.nextInt(numPages), numLowEngagedUserIndex)
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! unlikePage(Random.nextInt(numPages), numHighEngagedUserIndex)
      }

    }

    case simulateReadPageFeed(actorType: String) => {
      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(Random.nextInt(numMultiMediaSavyUsers)) ! getPageFeed(Random.nextInt(numPages))
        case "C2" => lowEngagedUsers(Random.nextInt(numLowEngagedUsers)) ! getPageFeed(Random.nextInt(numPages))
        case "C3" => textSavyPages(Random.nextInt(numTextSavyPages)) ! getPageFeed(Random.nextInt(numPages))
        case "C4" => highEngagedUsers(Random.nextInt(numHighEngagedUsers)) ! getPageFeed(Random.nextInt(numPages))
        case "C5" => multiMediaSpecialistPages(Random.nextInt(numMultiMediaSpecialistPages)) ! getPageFeed(Random.nextInt(numPages))
      }
    }
    case simulateDeletePagePost(actorType: String) => {
      var textSavyPageIndex = Random.nextInt(numTextSavyPages)
      var multiMediaSpecialistPageIndex = Random.nextInt(numMultiMediaSpecialistPages)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C3" => textSavyPages(textSavyPageIndex) ! deletePagePost(textSavyPageIndex, Random.nextInt(10) + 100)
        case "C5" => multiMediaSpecialistPages(multiMediaSpecialistPageIndex) ! deletePagePost(multiMediaSpecialistPageIndex, Random.nextInt(10) + 100)
      }

    }

    case simulateapproveDeclineRequest(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! approveDeclineRequest(numMultiMediaSavyUserIndex, Random.nextInt(numUsers), true)
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! approveDeclineRequest(numLowEngagedUserIndex, Random.nextInt(numUsers), true)
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! approveDeclineRequest(numHighEngagedUserIndex, Random.nextInt(numUsers), true)
      }

    }

    case simulateReadUserFeed(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)
      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! getEncryptedUserFeed(numMultiMediaSavyUserIndex)
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! getEncryptedUserFeed(numLowEngagedUserIndex)
        //case "C3" => textSavyPages(Random.nextInt(numTextSavyPages)) ! getEncryptedUserFeed(Random.nextInt(numUsers))
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! getEncryptedUserFeed(numHighEngagedUserIndex)
        //case "C5" => multiMediaSpecialistPages(Random.nextInt(numMultiMediaSpecialistPages)) ! getEncryptedUserFeed(Random.nextInt(numUsers))
      }

    }
    case simulatedeleteUserPost(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! deleteUserPost(numMultiMediaSavyUserIndex, numMultiMediaSavyUserIndex, Random.nextInt(10) + 100)
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! deleteUserPost(numLowEngagedUserIndex, numLowEngagedUserIndex, Random.nextInt(10) + 100)
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! deleteUserPost(numHighEngagedUserIndex, numHighEngagedUserIndex, Random.nextInt(10) + 100)
      }

    }

    case simulategetFriendList(actorType: String) => {
      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(Random.nextInt(numMultiMediaSavyUsers)) ! getFriendList(Random.nextInt(numUsers))
        case "C2" => lowEngagedUsers(Random.nextInt(numLowEngagedUsers)) ! getFriendList(Random.nextInt(numUsers))
        case "C3" => textSavyPages(Random.nextInt(numTextSavyPages)) ! getFriendList(Random.nextInt(numUsers))
        case "C4" => highEngagedUsers(Random.nextInt(numHighEngagedUsers)) ! getFriendList(Random.nextInt(numUsers))
        case "C5" => multiMediaSpecialistPages(Random.nextInt(numMultiMediaSpecialistPages)) ! getFriendList(Random.nextInt(numUsers))
      }

    }

    case simulategetFriendRequestList(actorType: String) => {
      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(Random.nextInt(numMultiMediaSavyUsers)) ! getFriendRequestList(Random.nextInt(numUsers))
        case "C2" => lowEngagedUsers(Random.nextInt(numLowEngagedUsers)) ! getFriendRequestList(Random.nextInt(numUsers))
        case "C3" => textSavyPages(Random.nextInt(numTextSavyPages)) ! getFriendRequestList(Random.nextInt(numUsers))
        case "C4" => highEngagedUsers(Random.nextInt(numHighEngagedUsers)) ! getFriendRequestList(Random.nextInt(numUsers))
        case "C5" => multiMediaSpecialistPages(Random.nextInt(numMultiMediaSpecialistPages)) ! getFriendRequestList(Random.nextInt(numUsers))
      }

    }

    case simulategettUserPicture(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => {
          if (numMultiMediaSavyUserIndex % 10 == 0) {
            multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! getUserPicture(numMultiMediaSavyUserIndex, Random.nextInt(5))
          }
        }
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! getUserPicture(numLowEngagedUserIndex, Random.nextInt(5))
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! getUserPicture(numHighEngagedUserIndex, Random.nextInt(5))
      }

    }

    case simulategetPagePicture(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! getPagePicture(numMultiMediaSavyUserIndex, Random.nextInt(10))
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! getPagePicture(numLowEngagedUserIndex, Random.nextInt(10))
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! getPagePicture(numHighEngagedUserIndex, Random.nextInt(10))

      }

    }

    case simulationPostPictureandfriendRequest(index: Int, actorType: String) => {

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => {
          multiMediaSavyUsers(index) ! userEncryptedPost(Random.nextInt(numMultiMediaSavyUsers), index)
          multiMediaSavyUsers(index) ! sendFriendRequest(Random.nextInt(numMultiMediaSavyUsers), index)
          //multiMediaSavyUsers(index) ! postUserPicture(index, Random.nextInt(10))
          if (index % 10 == 0) {
            multiMediaSavyUsers(index) ! postUserPicture(index, Random.nextInt(5))
            multiMediaSavyUsers(index) ! sendFriendRequest(Random.nextInt(numMultiMediaSavyUsers), index)
          }

        }
        case "C2" => {
          lowEngagedUsers(index) ! userEncryptedPost(Random.nextInt(numMultiMediaSavyUsers), index)
          if (index % 10 == 0)
            lowEngagedUsers(index) ! sendFriendRequest(Random.nextInt(numLowEngagedUsers), index)
        }
        case "C3" => {
          textSavyPages(index) ! pagePost(index)

        }
        case "C4" => {
          highEngagedUsers(index) ! userEncryptedPost(Random.nextInt(numMultiMediaSavyUsers), index)
          if (index % 10 == 0)
            highEngagedUsers(index) ! sendFriendRequest(Random.nextInt(numHighEngagedUsers), index)
        }
        case "C5" => {
          multiMediaSpecialistPages(index) ! pagePost(index)
          multiMediaSpecialistPages(index) ! postPagePicture(index, Random.nextInt(10))
          if (index % 20 == 0)
            multiMediaSpecialistPages(index) ! postPagePicture(index, Random.nextInt(10))

        }
      }

    }

    case stop() => {
      println("here")
      context.stop(self)
    }

  }

}

class Client(system: ActorSystem) extends Actor {
  import system.dispatcher
  var userProfileKey: SecretKey = null
  var pageProfileKey: SecretKey = null
  var pagepostId: Int = -1
  var userpostId: Int = -1
  var userFriendListArray: JSONArray = null
  var pagePostKeys = scala.collection.mutable.Map[Int, SecretKey]()
  var userPostKeys = scala.collection.mutable.Map[Int, SecretKey]()
  var pagePictureKeys = scala.collection.mutable.Map[Int, SecretKey]()
  var userPictureKeys = scala.collection.mutable.Map[Int, SecretKey]()
  var multiMediaSavyUsers = new ArrayBuffer[ActorRef]()
  var lowEngagedUsers = new ArrayBuffer[ActorRef]()
  var textSavyPages = new ArrayBuffer[ActorRef]()
  var highEngagedUsers = new ArrayBuffer[ActorRef]()
  var multiMediaSpecialistPages = new ArrayBuffer[ActorRef]()
  var initVector: String = ""
  var publicKey: PublicKey = null
  var privateKey: PrivateKey = null
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  val pipeline1: HttpRequest => Future[HttpResponse] = sendReceive
  var token = ""
  var pageName = ""
  var name = ""
  var base64String = ""
  val gender = Array("male", "female")
  var genderJson = ""

  def receive = {
    case RegisterUser(userId: Int) => {
      initVector = "AAAAAAAAAAAAAAAA"

      genderJson = Random.shuffle(gender.toList).head

      var keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048)
      var keyPair = keyPairGenerator.generateKeyPair()
      publicKey = keyPair.getPublic()
      privateKey = keyPair.getPrivate()

      var username = "Nikhil" + Random.alphanumeric.take(Random.nextInt(50)).mkString
      var keyGen: KeyGenerator = KeyGenerator.getInstance("AES");
      keyGen.init(128);
      userProfileKey = keyGen.generateKey();
      name = encrypt(username, userProfileKey)
      var data = SerializationUtils.serialize(publicKey)
      base64String = Base64.encodeBase64String(data)
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=" + userId + "&name=" + Random.alphanumeric.take(Random.nextInt(50)).mkString + "&gender=" + Random.shuffle(gender.toList).head))
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser", HttpEntity(MediaTypes.`application/json`, s"""{
        "userId": $userId,
        "name" : "$name", 
        "gender": "$genderJson",
        "userPublicKey" : "$base64String"
    }""")))

      response onSuccess {
        case result => {
          context.system.scheduler.schedule(FiniteDuration(30000, TimeUnit.MILLISECONDS), FiniteDuration(1000, TimeUnit.MILLISECONDS), self, updateFriendList(userId))
        }
      }
    }
    case RegisterPage(pageId: Int) => {
      initVector = "AAAAAAAAAAAAAAAA"
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerPage?pageId=" + pageId + "&pageName=" + Random.alphanumeric.take(Random.nextInt(50)).mkString))

      var keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048)
      var keyPair = keyPairGenerator.generateKeyPair()
      publicKey = keyPair.getPublic()
      privateKey = keyPair.getPrivate()

      var pageName = "Pageee" + Random.alphanumeric.take(Random.nextInt(50)).mkString
      var keyGen: KeyGenerator = KeyGenerator.getInstance("AES");
      keyGen.init(128);
      pageProfileKey = keyGen.generateKey();
      name = encrypt(pageName, pageProfileKey)
      var data = SerializationUtils.serialize(publicKey)
      base64String = Base64.encodeBase64String(data)
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerPage", HttpEntity(MediaTypes.`application/json`, s"""{
        "pageId": $pageId,
        "pageName" : "$name", 
        "likes": 0,
         "pagePublicKey" : "$base64String"
    }""")))

    }

    case LoginUser(userId: Int) => {
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/loginUser", HttpEntity(MediaTypes.`application/json`, s"""{
        "userId": $userId,
        "name" : "$name", 
        "gender": "$genderJson",
        "userPublicKey" : "$base64String"
    }""")))
        
         response onSuccess {
        case result => {
          
           token = decryptToken(result.entity.asString, privateKey, publicKey)
           val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
            val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/loginVerified"))

        }
      } 

    }

    case LoginPage(pageId: Int) => {
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/loginPage", HttpEntity(MediaTypes.`application/json`, s"""{
        "pageId": $pageId,
        "pageName" : "$name", 
        "likes": 0,
         "pagePublicKey" : "$base64String"
    }""")))

       response onSuccess {
        case result => {
           token = decryptToken(result.entity.asString, privateKey, publicKey)
           val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
            val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/loginVerified"))

        }
      }  
        

    }
    
       case LogoutUser(userId: Int) => {
     val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/logout"))
      
      response.foreach(
        response => {
         println(response.entity.asString)
          //println("TOKEN " + token)
        })
    }
       
    case GetUser(userId: Int) => {
      var userObject: JSONObject = null
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId))
      var userName = ""
      var responseUserId = 0
      response onSuccess {
        case result => {
          if (!result.entity.asString.contains("!")) {
            userObject = new JSONObject(result.entity.asString)
            userName = userObject.get("name").toString()
            responseUserId = userObject.get("userId").toString.toInt
            implicit val timeout = Timeout(5 seconds)
            var future: Future[Array[Byte]] = ask(getuserRef(responseUserId), getUserProfileKey(publicKey)).mapTo[Array[Byte]]
            future onSuccess {
              case result => {
                var decrptedUserProfileKey = decryptRSA(result, privateKey)
                userObject.remove("name");
                userObject.put("name", decrypt(userName, decrptedUserProfileKey));
                println(userObject.toString())
              }

            }
          } else { println(result.entity.asString) }
        }

      }

    }

    case GetPage(pageId: Int) => {
      var pageObject: JSONObject = null
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      var pageName = ""
      var responsePageId = 0
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/page/" + pageId))
      response onSuccess {
        case result => {
          pageObject = new JSONObject(result.entity.asString)
          pageName = pageObject.get("pageName").toString()
          responsePageId = pageObject.get("pageId").toString.toInt
          implicit val timeout = Timeout(5 seconds)
          var future: Future[Array[Byte]] = ask(getpageRef(responsePageId), getPageProfileKey(publicKey)).mapTo[Array[Byte]]
          future onSuccess {
            case result => {
              var decrptedPageProfileKey = decryptRSA(result, privateKey)
              pageObject.remove("pageName");
              pageObject.put("pageName", decrypt(pageName, decrptedPageProfileKey));
              println(pageObject.toString())
            }

          }

        }

      }
    }

    case likePage(pageId: Int, userId: Int) => {
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/likePage?pageId=" + pageId + "&userId=" + userId))
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/likePage", HttpEntity(MediaTypes.`application/json`, s"""{
        "pageId": $pageId,
        "userId" : $userId
    }""")))

    }

    case unlikePage(pageId: Int, userId: Int) => {
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/unlikePage?pageId=" + pageId + "&userId=" + userId))
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/unlikePage", HttpEntity(MediaTypes.`application/json`, s"""{
        "pageId": $pageId,
        "userId" : $userId
    }""")))

    }

    case pagePost(pageId: Int) => {
      //val response: Future[HttpResponse] = pipeline1(Post("http://localhost:8080/pagePost?pageId=" + pageId + "&post=" + Random.alphanumeric.take(Random.nextInt(140)).mkString))
      var pagepost = "PagePost" + Random.alphanumeric.take(Random.nextInt(50)).mkString
      pagepostId = pagepostId + 1
      var keyGen: KeyGenerator = KeyGenerator.getInstance("AES");
      keyGen.init(128);
      var aesKey = keyGen.generateKey()
      pagePostKeys += pagepostId -> aesKey
      var post = encrypt(pagepost, aesKey)
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/pagePost", HttpEntity(MediaTypes.`application/json`, s"""{
        "pageId": $pageId,
        "postId": $pagepostId,
        "post" : "$post"
    }""")))

    }

    case getPageFeed(pageId: Int) => {
      var pageFeedArray: JSONArray = null
      var pageAdminId: Int = 0
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)

      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/page/" + pageId + "/feed"))
      response onSuccess {
        case result => {
          if (!result.entity.asString.contains("!")) {
            pageFeedArray = new JSONArray(result.entity.asString)
            pageAdminId = pageFeedArray.getJSONObject(0).get("admin_creator").toString.toInt
            implicit val timeout = Timeout(5 seconds)
            var future: Future[scala.collection.mutable.Map[Int, Array[Byte]]] = ask(getpageRef(pageAdminId), getPagePostKeys(publicKey)).mapTo[scala.collection.mutable.Map[Int, Array[Byte]]]
            future onSuccess {
              case result => {
                println("Page Feed for page id " + pageAdminId + ":")
                if (result.size == pageFeedArray.length()) {
                  for (i <- 0 until result.size) {
                    var tempPostId = pageFeedArray.getJSONObject(i).get("postId").toString.toInt
                    var tempSecretKey = decryptRSA(result(tempPostId), privateKey)
                    println(decrypt(pageFeedArray.getJSONObject(i).get("post").toString, tempSecretKey))
                  }
                }
              }
            }
          } else {
            println("Page Feed: " + result.entity.asString)
          }

        }

      }
    }

    case deletePagePost(pageId: Int, postId: Int) => {
      // val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/deletePost?pageId=" + pageId + "&postId=" + postId))
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/deletePost", HttpEntity(MediaTypes.`application/json`, s"""{
        "pageId": $pageId,
        "postId" : $postId
    }""")))
    }

    case userPost(userId: Int, fromUser: Int) => {
      //val response: Future[HttpResponse] = pipeline1(Post("http://localhost:8080/userPost?userId=" + userId + "&fromUser=" + fromUser + "&post=" + Random.alphanumeric.take(Random.nextInt(140)).mkString))
      var userpost = "UserPost" + Random.alphanumeric.take(Random.nextInt(50)).mkString
      userpostId = userpostId + 1
      var keyGen: KeyGenerator = KeyGenerator.getInstance("AES");
      keyGen.init(128);
      var aesKey = keyGen.generateKey()
      userPostKeys += userpostId -> aesKey
      var post = encrypt(userpost, aesKey)
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/userPost", HttpEntity(MediaTypes.`application/json`, s"""{
        "userId": $userId,
        "fromUser" : $fromUser,
        "postId" : $userpostId,
        "post" : "$post"
    }""")))

    }
    case userEncryptedPost(userId: Int, fromUser: Int) => {
      //val response: Future[HttpResponse] = pipeline1(Post("http://localhost:8080/userPost?userId=" + userId + "&fromUser=" + fromUser + "&post=" + Random.alphanumeric.take(Random.nextInt(140)).mkString))
      var userpost = "UserPost" + Random.alphanumeric.take(Random.nextInt(50)).mkString
      var userObject: JSONObject = null
      userpostId = userpostId + 1
      var keyGen: KeyGenerator = KeyGenerator.getInstance("AES");
      keyGen.init(128);
      var aesKey = keyGen.generateKey()

      var encryptedPost = encrypt(userpost, aesKey)
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/getPublicKey/" + userId))
      response onSuccess {
        case result => {
          if (!result.entity.asString.contains("!")) {
            userObject = new JSONObject(result.entity.asString)
            var usrPublicKey = userObject.get("userPublicKey").toString()
            var decodedBase64String = Base64.decodeBase64(usrPublicKey.getBytes())
            var friendPublicKey: java.security.PublicKey = SerializationUtils.deserialize(decodedBase64String).asInstanceOf[java.security.PublicKey]
            var encryptedAesKey = encryptRSA(aesKey, friendPublicKey)
            var encryptedAesKeyString = Base64.encodeBase64String(encryptedAesKey);

            val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/userEncryptedPost", HttpEntity(MediaTypes.`application/json`, s"""{
              "userId": $userId,
              "fromUser" : $fromUser,
              "postId" : $userpostId,
              "post" : "$encryptedPost",
              "key" : "$encryptedAesKeyString"
          }""")))

          } else {
            println("User Feed: " + result.entity.asString)
          }

        }
      }

    }

    case getUserFeed(userId: Int) => {
      var userFeedArray: JSONArray = null
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/feed"))
      response onSuccess {
        case result => {
          if (!result.entity.asString.contains("!")) {
            userFeedArray = new JSONArray(result.entity.asString)
            implicit val timeout = Timeout(10 seconds)
            var future: Future[scala.collection.mutable.Map[Int, Array[Byte]]] = ask(getuserRef(userId), getUserPostKeys(userId, publicKey)).mapTo[scala.collection.mutable.Map[Int, Array[Byte]]]
            future onSuccess {
              case result => {
                if (result.size > 0) {
                  println("User Feed for user id " + userId + ":")
                  if (result.size == userFeedArray.length()) {
                    for (i <- 0 until result.size) {
                      var tempPostId = userFeedArray.getJSONObject(i).get("postId").toString.toInt
                      var tempSecretKey = decryptRSA(result(tempPostId), privateKey)
                      println(decrypt(userFeedArray.getJSONObject(i).get("post").toString, tempSecretKey))
                    }
                  }
                }

              }
            }
          } else {
            println("User Feed: " + result.entity.asString)
          }

        }

      }

    }

    case getEncryptedUserFeed(userId: Int) => {
      var userFeedArray: JSONArray = null
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/encryptedFeed"))
      response onSuccess {
        case result => {
          if (!result.entity.asString.contains("!")) {
            userFeedArray = new JSONArray(result.entity.asString)
            println("User Feed for user id " + userId + ":")
            //println(userFeedArray)
            for (i <- 0 until userFeedArray.length()) {
              var encryptedPost = userFeedArray.getJSONObject(i).get("post").toString()
              var encryptedAesKey = userFeedArray.getJSONObject(i).get("key").toString()
              var decryptAESKey = decryptRSA(Base64.decodeBase64(encryptedAesKey.getBytes()), privateKey)
              println(decrypt(encryptedPost, decryptAESKey))
            }
          } else {
            println(result.entity.asString)
          }

        }

      }

    }
    case deleteUserPost(userId: Int, fromUser: Int, postId: Int) => {
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/deletePost?userId=" + userId + "&fromUser=" + fromUser + "&postId=" + postId))
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/deletePost", HttpEntity(MediaTypes.`application/json`, s"""{
        "userId": $userId,
        "fromUser" : $fromUser,
        "postId" : $postId
    }""")))
      //      response.foreach(
      //              response =>
      //                println(s"\n${response.entity.asString}"))
    }

    case getFriendList(userId: Int) => {
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/friendsList"))
      //      response.foreach(
      //        response =>
      //          println(s"Friend List :\n${response.entity.asString}"))
    }

    case getFriendRequestList(userId: Int) => {
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/friendRequestsList"))
      //      response.foreach(
      //        response =>
      //          println(s"Friend Request List :\n${response.entity.asString}"))
    }

    case sendFriendRequest(userId: Int, friendId: Int) => {
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/friendRequest?userId=" + userId + "&friendId=" + friendId))
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/friendRequest", HttpEntity(MediaTypes.`application/json`, s"""{
        "userId": $userId,
        "friendId" : $friendId
    }""")))

    }

    case approveDeclineRequest(userId: Int, friendId: Int, decision: Boolean) => {
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/approveDeclineRequest?userId=" + userId + "&friendId=" + friendId + "&decision=" + decision))
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/approveDeclineRequest", HttpEntity(MediaTypes.`application/json`, s"""{
        "userId": $userId,
        "friendId" : $friendId,
        "decision" : $decision
    }""")))
      //      response.foreach(
      //              response =>
      //                println(s"\n${response.entity.asString}"))
    }

    case postUserPicture(userId: Int, pictureId: Int) => {

      var userObject: JSONObject = null
      val byteArray = Files.readAllBytes(Paths.get("src/abc.jpg"))
      var keyGen: KeyGenerator = KeyGenerator.getInstance("AES");
      keyGen.init(128);
      var aesKey = keyGen.generateKey()
      //userPictureKeys += pictureId -> aesKey
      val picturebase64String = Base64.encodeBase64String(byteArray);
      var base64String = encrypt(picturebase64String, aesKey)
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/getPublicKey/" + userId))
      response onSuccess {
        case result => {
          if (!result.entity.asString.contains("!")) {
            userObject = new JSONObject(result.entity.asString)
            var usrPublicKey = userObject.get("userPublicKey").toString()
            var decodedBase64String = Base64.decodeBase64(usrPublicKey.getBytes())
            var friendPublicKey: java.security.PublicKey = SerializationUtils.deserialize(decodedBase64String).asInstanceOf[java.security.PublicKey]
            var encryptedAesKey = encryptRSA(aesKey, friendPublicKey)
            var encryptedAesKeyString = Base64.encodeBase64String(encryptedAesKey);

            val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/userAlbum", HttpEntity(MediaTypes.`application/json`, s"""{
        "userId": "$userId",
        "pictureId" : "$pictureId", 
        "Image": "$base64String",
        "key":"$encryptedAesKeyString"
    }""")))

          } else {
            println(result.entity.asString)
          }

        }
      }

    }

    case getUserPicture(userId: Int, pictureId: Int) => {
      var userPictureObj: JSONObject = null
      var userPicture = ""
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/picture/" + pictureId))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
      response onSuccess {
        case result => {
          if (!result.entity.asString.contains("!")) {
            userPictureObj = new JSONObject(result.entity.asString)
            userPicture = userPictureObj.get("Image").toString()
            var encryptedAesKey = userPictureObj.get("key").toString()
            var tempSecretKey = decryptRSA(Base64.decodeBase64(encryptedAesKey.getBytes()), privateKey)
            println(decrypt(userPicture, tempSecretKey))
          } else {
            println("Picture: " + result.entity.asString)
          }
        }
      }
    }

    case postPagePicture(pageId: Int, pictureId: Int) => {

      val byteArray = Files.readAllBytes(Paths.get("src/abc.jpg"))
      //println(byteArray)
      var keyGen: KeyGenerator = KeyGenerator.getInstance("AES");
      keyGen.init(128);
      var aesKey = keyGen.generateKey()
      pagePictureKeys += pictureId -> aesKey
      val picturebase64String = Base64.encodeBase64String(byteArray);

      var base64String = encrypt(picturebase64String, aesKey)
      //println(pageId + "---------------" + base64String)
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/pageAlbum", HttpEntity(MediaTypes.`application/json`, s"""{
        "pageId": "$pageId",
        "pictureId" : "$pictureId", 
        "Image": "$base64String"
    }""")))

    }

    case getPagePicture(pageId: Int, pictureId: Int) => {
      var pagePictureObj: JSONObject = null
      var pageAdminId: Int = 0
      var pagePicture = ""
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/page/" + pageId + "/picture/" + pictureId))
      response onSuccess {
        case result => {
          if (!result.entity.asString.contains("!")) {
            pagePictureObj = new JSONObject(result.entity.asString)
            pagePicture = pagePictureObj.get("Image").toString()
            implicit val timeout = Timeout(5 seconds)
            var future: Future[Array[Byte]] = ask(getpageRef(pageId), getPagePictureKey(pictureId, publicKey)).mapTo[Array[Byte]]
            future onSuccess {
              case result => {
                var decrptedPagePictureKey = decryptRSA(result, privateKey)
                println(decrypt(pagePicture, decrptedPagePictureKey))
              }
            }
          } else {
            println("Picture: " + result.entity.asString)
          }
        }
      }
    }

    case statistics() => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/Statistics"))
      response.foreach(
        response =>
          println(s"Simulation Statistics for 60 Seconds  :\n${response.entity.asString}"))

    }

    case setOtherActorRef(multiMediaSavyActors: ArrayBuffer[ActorRef], lowEngagedActors: ArrayBuffer[ActorRef], textSavyActors: ArrayBuffer[ActorRef], highEngagedActors: ArrayBuffer[ActorRef], multiMediaSpecialistActors: ArrayBuffer[ActorRef]) => {
      multiMediaSavyUsers = multiMediaSavyActors
      lowEngagedUsers = lowEngagedActors
      textSavyPages = textSavyActors
      highEngagedUsers = highEngagedActors
      multiMediaSpecialistPages = multiMediaSpecialistActors

    }

    case getUserProfileKey(userPublicKey: PublicKey) => {
      var encryptedUserProfileKey = encryptRSA(userProfileKey, userPublicKey)
      sender ! encryptedUserProfileKey
    }
    case getPageProfileKey(pagePublicKey: PublicKey) => {
      var encryptedPageProfileKey = encryptRSA(pageProfileKey, pagePublicKey)
      sender ! encryptedPageProfileKey
    }

    case getPagePostKeys(pagePublicKey: PublicKey) => {
      var encryptedPagePostKeys = scala.collection.mutable.Map[Int, Array[Byte]]()
      for (i <- 0 until pagePostKeys.size) {
        encryptedPagePostKeys += i -> encryptRSA(pagePostKeys(i), pagePublicKey)
      }
      sender ! encryptedPagePostKeys
    }
    case getPagePictureKey(pictureId: Int, pagePublicKey: PublicKey) => {
      if (pagePictureKeys.contains(pictureId)) {
        var encryptedPagePictureKey = encryptRSA(pagePictureKeys(pictureId), pagePublicKey)
        sender ! encryptedPagePictureKey
      }
    }

    case updateFriendList(userId: Int) => {
      val pipeline: HttpRequest => Future[HttpResponse] = (
        addHeader("token", token)
        ~> sendReceive)
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/friendsList"))
      response onSuccess {
        case result => {
          if (!result.entity.asString.contains("!")) {
            userFriendListArray = new JSONArray(result.entity.asString)
            //println("----"+userFriendListArray)
          } else {
            //println("No friends found!")
          }
        }
      }
    }
    case getUserPostKeys(userId: Int, userPublicKey: PublicKey) => {
      var encryptedUserPostKeys = scala.collection.mutable.Map[Int, Array[Byte]]()
      if (userFriendListArray != null) {
        //println("friendlst:" + userFriendListArray)
        for (i <- 0 until userFriendListArray.length()) {
          if (userFriendListArray.getJSONObject(i).get("userId").toString().toInt == userId) {
            for (i <- 0 until userPostKeys.size) {
              encryptedUserPostKeys += i -> encryptRSA(userPostKeys(i), userPublicKey)
            }
          }
        }
      }
      sender ! encryptedUserPostKeys
    }
    case getUserPictureKey(userId: Int, pictureId: Int, userPublicKey: PublicKey) => {
      var encryptedUserPictureKey: Array[Byte] = Array()
      if (userFriendListArray != null) {
        //println("friendlst:" + userFriendListArray)
        for (i <- 0 until userFriendListArray.length()) {
          if (userFriendListArray.getJSONObject(i).get("userId").toString().toInt == userId) {
            if (userPictureKeys.contains(pictureId)) {
              encryptedUserPictureKey = encryptRSA(userPictureKeys(pictureId), userPublicKey)
            }
          }
        }
      }
      sender ! encryptedUserPictureKey

    }

  }

  def encrypt(stringToEncrypt: String, encryptionKey: SecretKey): String = {
    var cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new IvParameterSpec(initVector.getBytes("UTF-8")));
    var encryptedInput: Array[Byte] = cipher.doFinal(stringToEncrypt.getBytes("UTF-8"));
    return Base64.encodeBase64String(encryptedInput);

  }
  def decrypt(stringToDecrypt: String, encryptionKey: SecretKey): String = {
    var cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new IvParameterSpec(initVector.getBytes("UTF-8")));
    return new String(cipher.doFinal(Base64.decodeBase64(stringToDecrypt)), "UTF-8");

  }
  def encryptRSA(symKey: javax.crypto.SecretKey, key: PublicKey): Array[Byte] = {
    var data = SerializationUtils.serialize(symKey);
    var xform = "RSA/ECB/PKCS1Padding";
    var cipher = Cipher.getInstance(xform);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(data);
  }

  def decryptRSA(inputBytes: Array[Byte], key: PrivateKey): javax.crypto.SecretKey = {
    var xform = "RSA/ECB/PKCS1Padding";
    var cipher = Cipher.getInstance(xform);
    cipher.init(Cipher.DECRYPT_MODE, key);
    var decBytes = cipher.doFinal(inputBytes);
    return SerializationUtils.deserialize(decBytes).asInstanceOf[javax.crypto.SecretKey]
  }

  def decryptToken(cipherText: String, key: PrivateKey, key1: PublicKey): String = {
    var cipher = Cipher.getInstance("RSA");

    cipher.init(Cipher.DECRYPT_MODE, key);
    var ciphertextBytes = Base64.decodeBase64(cipherText.getBytes("ISO-8859-1"));
    var decryptedBytes = cipher.doFinal(ciphertextBytes);
    //println("Decrpyred btyes " + decryptedBytes)
    return new String(decryptedBytes, "ISO-8859-1")
  }

  def getuserRef(userId: Int): ActorRef = {
    var numMultiMediaSavyUsers = multiMediaSavyUsers.size
    var numLowEngagedUsers = lowEngagedUsers.size
    var numHighEngagedUsers = highEngagedUsers.size
    if (userId < numMultiMediaSavyUsers) {
      return multiMediaSavyUsers(userId)
    } else if (userId >= numMultiMediaSavyUsers && userId < (numLowEngagedUsers + numMultiMediaSavyUsers)) {
      return lowEngagedUsers(userId - numMultiMediaSavyUsers)
    } else { //if (userId >= (numLowEngagedUsers + numMultiMediaSavyUsers) && userId <= (numLowEngagedUsers + numMultiMediaSavyUsers + numHighEngagedUsers)) {
      return highEngagedUsers(userId - numMultiMediaSavyUsers - numLowEngagedUsers)
    }

  }

  def getpageRef(pageId: Int): ActorRef = {
    var numTextSavyPages = textSavyPages.size
    var numMultiMediaSpecialistPages = multiMediaSpecialistPages.size
    if (pageId < numTextSavyPages) {
      return textSavyPages(pageId)
    } else {
      return multiMediaSpecialistPages(pageId - numTextSavyPages)
    }
  }

}
