# SpringBinder is a binder of objects to JPA entities

With spring-binder you'd get rid of writing the boilerplate codes
to map posted dto objects to jpa entity models and save them to database.
It supports both x-www-form-urlencoded/multipart forms and REST requests.
 
Have you ever used or heard libraries like [ModelMapper](http://modelmapper.org/)?
There are samples to map `DTO` objects to entities using `ModelMapper`.
Well, we do that in a more efficient and clever way. Using `ModelMapper` you need to first
desrialize data to `DTO` object then use `ModelMapper` to access properties and create mapping.
This is somehow inefficient because the reflection is applied two times.
With `SpringBinder`, wile the object is getting deserilized, the mapping is applied.
Besides that, there are some other cool features like using `entity graph` and serilize/deserilize 
customization
 