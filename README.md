# ServerPoolProject

The essence of the project is to train concurrency, the project is named a server pool in which it will allocate some computation power or memory on the cloud via a cloud provider.

The requirements are to Implement ResourceManagementService to accomplish the following, 
You have a cloud service provider that can provide any number of servers that have 100 Giga of memory  (as a maximum).
You should create/allocate servers are persisted in a DB of their choice 
the allocation should be done via RESTFul APIs 
Here is a detailed description 
you get a request from a user to allocate 30 Giga from your servers pool. you loop over your server pool in the database, if there is a server of 30G capacity, then you allocate the memory and update the record back into DB. 
if there isn't enough space in the already created servers pool that you own, you can spin a new one on the cloud.
Spinning a server takes a while, once spun it will be in creating state ( for 20seconds) and then will be in an active state
Allocation can be done only on active stated servers. 
please bear in mind that you might get simultaneous requests. 
here are some use cases that could help you understand the project is: 
CASE 1: suppose that I have a pool of three servers each has a free space of 30 Gigas. And then I was hit by simultaneous two requests in which each one of them needs 60 Gigas. Your program then should spin two servers, and allocate the 60 Giga for each request
  CASE 2: suppose that I have a pool of one server that has 30 Gigas. And then I was hit by two simultaneous requests in which each one of them needs 50 Gigas. Your program then should spin ONE server, and allocate the 50 Giga for each request  
Do Automation for the rest services they exposed via Robot framework 
validate positive and negative scenarios 
APIs should be validated 
They should validate the consistency in the database as well 
