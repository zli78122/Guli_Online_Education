# Guli Online Education

*Website: https://youtu.be/t-wol7UFu_s*  

## Introduction
* Developed a **B2C** online education website using a development model of separation of frontend and backend, the backend is a **microservice** architecture built by the **MVC design pattern** based on **Spring Boot** and **Spring Cloud**, and the frontend is based on **Vue.js**.
* Combined **content-based recommendation** and **item-based collaborative filtering recommendation** algorithms to train course recommendation models written in **PySpark**, which can match **98.24%** of similar item pairs in the ground truth.
* Predicted users’ ratings for courses based on the generated models using Pearson Correlation Coefficient and calculated **RMSE** equal to **0.899**.
* Integrated **Amazon S3** saving image objects and **Alibaba Cloud VoD** uploading and watching course videos for durability and availability.
* Created MySQL database on **Amazon RDS** and deployed the application to **Amazon EC2** for better performance.
