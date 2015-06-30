trait ContentMarshaller {
  def apply(content:String) : String
}

trait TwitterMarshaller extends ContentMarshaller{
  def apply(content:String) = {
    "This is twitter marshaller"
  }
}

trait XmlMarshaller extends ContentMarshaller{
  def apply(content:String) = {
    "This is xml marshaller"
  }
}

trait coba{
  this: ContentMarshaller =>
}

lazy val a = new coba with TwitterMarshaller
lazy val b = new coba with XmlMarshaller

a("magic")
b("magic")

val data = 111 :: "Hlalo" :: Nil

val num::string::Nil = data

num
string