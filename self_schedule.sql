-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: self_schedule
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'submit user id',
  `category` varchar(20) NOT NULL DEFAULT 'other' COMMENT 'feedback type: bug, feature, experience, other',
  `title` varchar(100) NOT NULL COMMENT 'feedback title',
  `content` text NOT NULL COMMENT 'feedback content',
  `contact` varchar(100) DEFAULT NULL COMMENT 'contact info',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0=pending, 1=processed, 2=closed',
  `admin_reply` text COMMENT 'admin reply',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_read` int DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback`
--

LOCK TABLES `feedback` WRITE;
/*!40000 ALTER TABLE `feedback` DISABLE KEYS */;
INSERT INTO `feedback` VALUES (1,5,'other','测试反馈功能','你好啊！未来注定之人','',1,'你也好','2026-05-12 19:06:35','2026-05-12 19:06:35',1),(2,5,'other','测试反馈小红点','你好管理员','',1,'你好','2026-05-12 19:20:26','2026-05-12 19:23:39',1);
/*!40000 ALTER TABLE `feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `focus_record`
--

DROP TABLE IF EXISTS `focus_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `focus_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `task_id` bigint DEFAULT NULL COMMENT '关联的任务ID(可为空)',
  `start_time` datetime NOT NULL COMMENT '专注开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '专注结束时间',
  `duration` int NOT NULL DEFAULT '0' COMMENT '专注时长(分钟)',
  `interruptions` int NOT NULL DEFAULT '0' COMMENT '被打断次数',
  `focus_score` tinyint DEFAULT NULL COMMENT '专注评分(1-5分)',
  `notes` text COLLATE utf8mb4_unicode_ci COMMENT '专注备注',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-已取消, 1-已完成, 2-中途放弃',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_task` (`user_id`,`task_id`) COMMENT '按用户和任务查询',
  KEY `idx_start_time` (`start_time`) COMMENT '按开始时间查询',
  KEY `idx_user_date` (`user_id`,`start_time`) COMMENT '按用户和日期查询',
  KEY `fk_focus_task` (`task_id`),
  KEY `idx_user_start_time` (`user_id`,`start_time`),
  CONSTRAINT `fk_focus_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_focus_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专注记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `focus_record`
--

LOCK TABLES `focus_record` WRITE;
/*!40000 ALTER TABLE `focus_record` DISABLE KEYS */;
INSERT INTO `focus_record` VALUES (55,4,315,'2026-05-14 18:45:45','2026-05-14 18:45:56',10,0,NULL,'',1,'2026-05-14 18:45:56'),(56,4,NULL,'2026-05-14 18:47:58','2026-05-14 18:49:09',60,0,NULL,'',1,'2026-05-14 18:49:09'),(57,4,316,'2026-05-14 19:52:10','2026-05-14 19:53:11',60,0,NULL,'',1,'2026-05-14 19:53:11');
/*!40000 ALTER TABLE `focus_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reminder_records`
--

DROP TABLE IF EXISTS `reminder_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reminder_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `task_id` bigint NOT NULL,
  `task_title` varchar(255) NOT NULL,
  `trigger_time` datetime NOT NULL,
  `task_time` datetime NOT NULL,
  `reminder_type` varchar(50) NOT NULL,
  `status` varchar(50) DEFAULT 'PENDING',
  `send_status` int DEFAULT '0',
  `message` text,
  `channel` varchar(50) DEFAULT 'SYSTEM',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `retry_count` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_trigger_time` (`trigger_time`),
  KEY `idx_status` (`status`),
  KEY `idx_status_send_status_trigger_time` (`status`,`send_status`,`trigger_time`)
) ENGINE=InnoDB AUTO_INCREMENT=427 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reminder_records`
--

LOCK TABLES `reminder_records` WRITE;
/*!40000 ALTER TABLE `reminder_records` DISABLE KEYS */;
INSERT INTO `reminder_records` VALUES (187,4,200,'洗漱','2026-05-08 22:00:00','2026-05-08 23:00:00','REMIND_TIME','SENT',1,'任务「洗漱」即将开始，时间：2026-05-08 23:00','SYSTEM','2026-05-08 19:02:04','2026-05-08 14:00:19',0),(213,4,200,'洗漱','2026-05-08 23:00:00','2026-05-08 23:00:00','REMIND_TIME','SENT',1,'任务「洗漱」即将开始，时间：2026-05-08 23:00','SYSTEM','2026-05-08 22:29:21','2026-05-08 22:29:21',0),(215,4,199,'撤退','2026-05-08 22:00:00','2026-05-08 22:00:00','REMIND_TIME','SENT',1,'任务「撤退」即将开始，时间：2026-05-08 22:00','SYSTEM','2026-05-08 22:48:30','2026-05-08 22:48:30',0),(233,4,206,'洗漱','2026-05-09 23:00:00','2026-05-09 23:00:00','REMIND_TIME','SENT',1,'任务「洗漱」即将开始，时间：2026-05-09 23:00','SYSTEM','2026-05-08 23:30:35','2026-05-09 23:00:07',0),(234,4,202,'撤退','2026-05-09 22:00:00','2026-05-09 22:00:00','REMIND_TIME','SENT',1,'任务「撤退」即将开始，时间：2026-05-09 22:00','SYSTEM','2026-05-08 23:30:39','2026-05-09 22:00:07',0),(235,4,195,'起床+学习408','2026-05-09 13:00:00','2026-05-09 13:00:00','REMIND_TIME','SENT',1,'任务「起床+学习408」即将开始，时间：2026-05-09 13:00','SYSTEM','2026-05-08 23:30:43','2026-05-09 13:00:08',0),(236,4,196,'吃饭','2026-05-09 17:15:00','2026-05-09 17:15:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-09 17:15','SYSTEM','2026-05-08 23:30:46','2026-05-09 17:15:09',0),(237,4,197,'查缺补漏','2026-05-09 18:15:00','2026-05-09 18:15:00','REMIND_TIME','SENT',1,'任务「查缺补漏」即将开始，时间：2026-05-09 18:15','SYSTEM','2026-05-08 23:30:50','2026-05-09 18:15:09',0),(238,4,198,'搓项目+吃药','2026-05-09 20:30:00','2026-05-09 20:30:00','REMIND_TIME','SENT',1,'任务「搓项目+吃药」即将开始，时间：2026-05-09 20:30','SYSTEM','2026-05-08 23:30:53','2026-05-09 20:30:08',0),(239,4,193,'吃饭','2026-05-09 11:00:00','2026-05-09 11:00:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-09 11:00','SYSTEM','2026-05-08 23:31:03','2026-05-09 12:33:28',0),(240,4,194,'睡觉','2026-05-09 12:30:00','2026-05-09 12:30:00','REMIND_TIME','SENT',1,'任务「睡觉」即将开始，时间：2026-05-09 12:30','SYSTEM','2026-05-08 23:31:08','2026-05-09 12:33:29',0),(241,4,191,'起床','2026-05-09 07:30:00','2026-05-09 07:30:00','REMIND_TIME','SENT',1,'任务「起床」即将开始，时间：2026-05-09 07:30','SYSTEM','2026-05-08 23:31:16','2026-05-09 12:33:28',0),(242,4,192,'数学备考+吃药','2026-05-09 08:00:00','2026-05-09 08:00:00','REMIND_TIME','SENT',1,'任务「数学备考+吃药」即将开始，时间：2026-05-09 08:00','SYSTEM','2026-05-08 23:31:22','2026-05-09 12:33:28',0),(272,4,223,'起床','2026-05-10 07:30:00','2026-05-10 07:30:00','REMIND_TIME','SENT',1,'任务「起床」即将开始，时间：2026-05-10 07:30','SYSTEM','2026-05-09 12:33:29','2026-05-10 16:53:07',0),(273,4,224,'数学备考+吃药','2026-05-10 08:00:00','2026-05-10 08:00:00','REMIND_TIME','SENT',1,'任务「数学备考+吃药」即将开始，时间：2026-05-10 08:00','SYSTEM','2026-05-09 12:33:29','2026-05-10 16:53:08',0),(274,4,225,'吃饭','2026-05-10 11:00:00','2026-05-10 11:00:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-10 11:00','SYSTEM','2026-05-09 12:33:29','2026-05-10 16:53:09',0),(275,4,226,'睡觉','2026-05-10 12:30:00','2026-05-10 12:30:00','REMIND_TIME','SENT',1,'任务「睡觉」即将开始，时间：2026-05-10 12:30','SYSTEM','2026-05-09 12:33:29','2026-05-10 16:53:09',0),(281,4,229,'起床+学习408','2026-05-10 13:00:00','2026-05-10 13:00:00','REMIND_TIME','SENT',1,'任务「起床+学习408」即将开始，时间：2026-05-10 13:00','SYSTEM','2026-05-09 13:00:09','2026-05-10 16:53:09',0),(282,4,230,'吃饭','2026-05-10 17:15:00','2026-05-10 17:15:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-10 17:15','SYSTEM','2026-05-09 17:15:10','2026-05-10 17:15:05',0),(299,4,238,'撤退','2026-05-10 22:00:00','2026-05-10 22:00:00','REMIND_TIME','SENT',1,'任务「撤退」即将开始，时间：2026-05-10 22:00','SYSTEM','2026-05-09 22:00:07','2026-05-10 22:12:28',0),(301,4,239,'洗漱','2026-05-10 23:00:00','2026-05-10 23:00:00','REMIND_TIME','SENT',1,'任务「洗漱」即将开始，时间：2026-05-10 23:00','SYSTEM','2026-05-09 23:00:08','2026-05-10 23:00:08',0),(302,4,240,'起床','2026-05-11 07:30:00','2026-05-11 07:30:00','REMIND_TIME','SENT',1,'任务「起床」即将开始，时间：2026-05-11 07:30','SYSTEM','2026-05-10 16:53:09','2026-05-11 09:20:39',0),(303,4,241,'数学备考+吃药','2026-05-11 08:00:00','2026-05-11 08:00:00','REMIND_TIME','SENT',1,'任务「数学备考+吃药」即将开始，时间：2026-05-11 08:00','SYSTEM','2026-05-10 16:53:09','2026-05-11 09:20:40',0),(304,4,242,'吃饭','2026-05-11 11:00:00','2026-05-11 11:00:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-11 11:00','SYSTEM','2026-05-10 16:53:10','2026-05-11 12:03:43',0),(305,4,243,'睡觉','2026-05-11 12:30:00','2026-05-11 12:30:00','REMIND_TIME','SENT',1,'任务「睡觉」即将开始，时间：2026-05-11 12:30','SYSTEM','2026-05-10 16:53:10','2026-05-11 12:30:10',0),(306,4,244,'起床+学习408','2026-05-11 13:00:00','2026-05-11 13:00:00','REMIND_TIME','SENT',1,'任务「起床+学习408」即将开始，时间：2026-05-11 13:00','SYSTEM','2026-05-10 16:53:10','2026-05-11 13:00:06',0),(307,4,245,'吃饭','2026-05-11 17:15:00','2026-05-11 17:15:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-11 17:15','SYSTEM','2026-05-10 17:15:06','2026-05-11 17:15:08',0),(311,4,231,'查缺补漏','2026-05-10 18:15:00','2026-05-10 18:15:00','REMIND_TIME','SENT',1,'任务「查缺补漏」即将开始，时间：2026-05-10 18:15','SYSTEM','2026-05-10 17:42:50','2026-05-10 18:15:02',0),(313,4,232,'搓项目+吃药','2026-05-10 20:30:00','2026-05-10 20:30:00','REMIND_TIME','SENT',1,'任务「搓项目+吃药」即将开始，时间：2026-05-10 20:30','SYSTEM','2026-05-10 17:57:48','2026-05-10 20:30:03',0),(314,4,249,'查缺补漏','2026-05-11 18:15:00','2026-05-11 18:15:00','REMIND_TIME','SENT',1,'任务「查缺补漏」即将开始，时间：2026-05-11 18:15','SYSTEM','2026-05-10 18:15:03','2026-05-11 18:15:04',0),(328,4,254,'手表充电','2026-05-10 21:00:00','2026-05-10 21:00:00','REMIND_TIME','SENT',1,'任务「手表充电」即将开始，时间：2026-05-10 21:00','SYSTEM','2026-05-10 19:47:14','2026-05-10 21:00:09',0),(329,4,255,'搓项目+吃药','2026-05-11 20:30:00','2026-05-11 20:30:00','REMIND_TIME','SENT',1,'任务「搓项目+吃药」即将开始，时间：2026-05-11 20:30','SYSTEM','2026-05-10 20:30:03','2026-05-11 21:05:17',0),(330,4,256,'手表充电','2026-05-17 21:00:00','2026-05-17 21:00:00','REMIND_TIME','PENDING',0,'任务「手表充电」即将开始，时间：2026-05-17 21:00','SYSTEM','2026-05-10 21:00:10','2026-05-10 21:00:10',0),(331,4,257,'撤退','2026-05-11 22:00:00','2026-05-11 22:00:00','REMIND_TIME','SENT',1,'任务「撤退」即将开始，时间：2026-05-11 22:00','SYSTEM','2026-05-10 22:12:30','2026-05-11 22:00:03',0),(332,4,258,'洗漱','2026-05-11 23:00:00','2026-05-11 23:00:00','REMIND_TIME','SENT',1,'任务「洗漱」即将开始，时间：2026-05-11 23:00','SYSTEM','2026-05-10 23:00:08','2026-05-11 23:00:10',0),(333,4,259,'起床','2026-05-12 07:30:00','2026-05-12 07:30:00','REMIND_TIME','SENT',1,'任务「起床」即将开始，时间：2026-05-12 07:30','SYSTEM','2026-05-11 09:20:40','2026-05-12 13:04:37',0),(334,4,260,'数学备考+吃药','2026-05-12 08:00:00','2026-05-12 08:00:00','REMIND_TIME','SENT',1,'任务「数学备考+吃药」即将开始，时间：2026-05-12 08:00','SYSTEM','2026-05-11 09:20:40','2026-05-12 13:04:37',0),(335,4,261,'吃饭','2026-05-12 11:00:00','2026-05-12 11:00:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-12 11:00','SYSTEM','2026-05-11 12:03:44','2026-05-12 13:04:38',0),(336,4,262,'睡觉','2026-05-12 12:30:00','2026-05-12 12:30:00','REMIND_TIME','SENT',1,'任务「睡觉」即将开始，时间：2026-05-12 12:30','SYSTEM','2026-05-11 12:30:10','2026-05-12 13:04:38',0),(337,4,263,'起床+学习408','2026-05-12 13:00:00','2026-05-12 13:00:00','REMIND_TIME','SENT',1,'任务「起床+学习408」即将开始，时间：2026-05-12 13:00','SYSTEM','2026-05-11 13:00:06','2026-05-12 13:04:38',0),(338,4,264,'吃饭','2026-05-12 17:15:00','2026-05-12 17:15:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-12 17:15','SYSTEM','2026-05-11 17:15:10','2026-05-12 17:15:04',0),(342,4,266,'查缺补漏','2026-05-12 18:15:00','2026-05-12 18:15:00','REMIND_TIME','READ',1,'任务「查缺补漏」即将开始，时间：2026-05-12 18:15','SYSTEM','2026-05-11 18:15:06','2026-05-12 18:15:02',0),(343,4,267,'搓项目+吃药','2026-05-12 20:30:00','2026-05-12 20:30:00','REMIND_TIME','SENT',1,'任务「搓项目+吃药」即将开始，时间：2026-05-12 20:30','SYSTEM','2026-05-11 21:05:18','2026-05-12 20:30:01',0),(344,4,268,'撤退','2026-05-12 22:00:00','2026-05-12 22:00:00','REMIND_TIME','READ',1,'任务「撤退」即将开始，时间：2026-05-12 22:00','SYSTEM','2026-05-11 22:00:04','2026-05-12 22:00:04',0),(345,4,269,'洗漱','2026-05-12 23:00:00','2026-05-12 23:00:00','REMIND_TIME','SENT',1,'任务「洗漱」即将开始，时间：2026-05-12 23:00','SYSTEM','2026-05-11 23:00:11','2026-05-12 23:00:05',0),(377,4,284,'起床','2026-05-13 07:30:00','2026-05-13 07:30:00','REMIND_TIME','SENT',1,'任务「起床」即将开始，时间：2026-05-13 07:30','SYSTEM','2026-05-12 13:04:38','2026-05-13 09:40:37',0),(379,4,286,'吃饭','2026-05-13 11:00:00','2026-05-13 11:00:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-13 11:00','SYSTEM','2026-05-12 13:04:39','2026-05-13 11:00:08',0),(380,4,287,'睡觉','2026-05-13 12:30:00','2026-05-13 12:30:00','REMIND_TIME','SENT',1,'任务「睡觉」即将开始，时间：2026-05-13 12:30','SYSTEM','2026-05-12 13:04:39','2026-05-13 12:30:08',0),(381,4,288,'起床+学习408','2026-05-13 13:00:00','2026-05-13 13:00:00','REMIND_TIME','SENT',1,'任务「起床+学习408」即将开始，时间：2026-05-13 13:00','SYSTEM','2026-05-12 13:04:39','2026-05-13 13:00:05',0),(382,4,289,'吃饭','2026-05-13 17:15:00','2026-05-13 17:15:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-13 17:15','SYSTEM','2026-05-12 17:15:06','2026-05-13 17:15:01',0),(387,4,285,'数学备考','2026-05-13 08:00:00','2026-05-13 08:00:00','REMIND_TIME','SENT',1,'任务「数学备考」即将开始，时间：2026-05-13 08:00','SYSTEM','2026-05-12 23:26:01','2026-05-13 09:40:37',0),(391,4,295,'数学备考','2026-05-14 08:00:00','2026-05-14 08:00:00','REMIND_TIME','SENT',1,'任务「数学备考」即将开始，时间：2026-05-14 08:00','SYSTEM','2026-05-13 09:40:38','2026-05-14 11:24:31',0),(392,4,296,'吃饭','2026-05-14 11:00:00','2026-05-14 11:00:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-14 11:00','SYSTEM','2026-05-13 11:00:10','2026-05-14 11:24:32',0),(393,4,297,'睡觉','2026-05-14 12:30:00','2026-05-14 12:30:00','REMIND_TIME','SENT',1,'任务「睡觉」即将开始，时间：2026-05-14 12:30','SYSTEM','2026-05-13 12:30:09','2026-05-14 12:30:05',0),(394,4,298,'起床+学习408','2026-05-14 13:00:00','2026-05-14 13:00:00','REMIND_TIME','SENT',1,'任务「起床+学习408」即将开始，时间：2026-05-14 13:00','SYSTEM','2026-05-13 13:00:06','2026-05-14 13:00:05',0),(395,4,299,'吃饭','2026-05-14 17:15:00','2026-05-14 17:15:00','REMIND_TIME','SENT',1,'任务「吃饭」即将开始，时间：2026-05-14 17:15','SYSTEM','2026-05-13 17:15:02','2026-05-14 17:15:05',0),(397,4,300,'查缺补漏','2026-05-14 18:15:00','2026-05-14 18:15:00','REMIND_TIME','SENT',1,'任务「查缺补漏」即将开始，时间：2026-05-14 18:15','SYSTEM','2026-05-13 17:26:14','2026-05-14 18:15:09',0),(404,4,292,'撤退','2026-05-13 22:00:00','2026-05-13 22:00:00','REMIND_TIME','SENT',1,'任务「撤退」即将开始，时间：2026-05-13 22:00','SYSTEM','2026-05-13 20:49:06','2026-05-13 22:00:08',0),(405,4,293,'洗漱','2026-05-13 23:00:00','2026-05-13 23:00:00','REMIND_TIME','SENT',1,'任务「洗漱」即将开始，时间：2026-05-13 23:00','SYSTEM','2026-05-13 20:49:12','2026-05-13 23:00:01',0),(406,4,294,'起床','2026-05-14 07:30:00','2026-05-14 07:30:00','REMIND_TIME','SENT',1,'任务「起床」即将开始，时间：2026-05-14 07:30','SYSTEM','2026-05-13 20:49:17','2026-05-14 11:24:31',0),(410,4,305,'撤退','2026-05-14 22:00:00','2026-05-14 22:00:00','REMIND_TIME','READ',0,'任务「撤退」即将开始，时间：2026-05-14 22:00','SYSTEM','2026-05-13 22:00:10','2026-05-14 22:00:07',1),(415,4,308,'起床','2026-05-15 07:30:00','2026-05-15 07:30:00','REMIND_TIME','PENDING',0,'任务「起床」即将开始，时间：2026-05-15 07:30','SYSTEM','2026-05-14 11:24:32','2026-05-14 11:24:32',0),(416,4,309,'数学备考','2026-05-15 08:00:00','2026-05-15 08:00:00','REMIND_TIME','PENDING',0,'任务「数学备考」即将开始，时间：2026-05-15 08:00','SYSTEM','2026-05-14 11:24:32','2026-05-14 11:24:32',0),(417,4,310,'吃饭','2026-05-15 11:00:00','2026-05-15 11:00:00','REMIND_TIME','PENDING',0,'任务「吃饭」即将开始，时间：2026-05-15 11:00','SYSTEM','2026-05-14 11:24:32','2026-05-14 11:24:32',0),(418,4,311,'睡觉','2026-05-15 12:30:00','2026-05-15 12:30:00','REMIND_TIME','PENDING',0,'任务「睡觉」即将开始，时间：2026-05-15 12:30','SYSTEM','2026-05-14 12:30:06','2026-05-14 12:30:06',0),(419,4,312,'起床+学习408','2026-05-15 13:00:00','2026-05-15 13:00:00','REMIND_TIME','PENDING',0,'任务「起床+学习408」即将开始，时间：2026-05-15 13:00','SYSTEM','2026-05-14 13:00:06','2026-05-14 13:00:06',0),(420,4,313,'吃饭','2026-05-15 17:15:00','2026-05-15 17:15:00','REMIND_TIME','PENDING',0,'任务「吃饭」即将开始，时间：2026-05-15 17:15','SYSTEM','2026-05-14 17:15:06','2026-05-14 17:15:06',0),(421,4,314,'查缺补漏','2026-05-15 18:15:00','2026-05-15 18:15:00','REMIND_TIME','PENDING',0,'任务「查缺补漏」即将开始，时间：2026-05-15 18:15','SYSTEM','2026-05-14 18:15:11','2026-05-14 18:15:11',0),(422,4,319,'撤退','2026-05-15 22:00:00','2026-05-15 22:00:00','REMIND_TIME','PENDING',0,'任务「撤退」即将开始，时间：2026-05-15 22:00','SYSTEM','2026-05-14 22:00:07','2026-05-14 22:00:07',0),(423,4,320,'洗漱','2026-05-15 23:00:00','2026-05-15 23:00:00','REMIND_TIME','PENDING',0,'任务「洗漱」即将开始，时间：2026-05-15 23:00','SYSTEM','2026-05-14 23:00:03','2026-05-14 23:00:03',0);
/*!40000 ALTER TABLE `reminder_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subtask`
--

DROP TABLE IF EXISTS `subtask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subtask` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '关联的父任务ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '子任务标题',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待办，2-完成',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序顺序',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `completed_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subtask`
--

LOCK TABLES `subtask` WRITE;
/*!40000 ALTER TABLE `subtask` DISABLE KEYS */;
INSERT INTO `subtask` VALUES (2,154,'设计网页布局',0,0,'2026-05-07 22:25:12',NULL),(3,154,'编写HTML代码',0,1,'2026-05-07 22:25:12',NULL),(4,154,'添加CSS样式',0,2,'2026-05-07 22:25:12',NULL),(5,154,'实现JavaScript交互',0,3,'2026-05-07 22:25:12',NULL),(6,155,'设计后端架构',0,0,'2026-05-07 22:25:12',NULL),(7,155,'编写后端代码',0,1,'2026-05-07 22:25:13',NULL),(8,155,'实现接口文档',0,2,'2026-05-07 22:25:13',NULL),(9,155,'数据库表设计',0,3,'2026-05-07 22:25:13',NULL),(10,156,'整理接口列表',0,0,'2026-05-07 22:25:13',NULL),(11,156,'描述接口参数',0,1,'2026-05-07 22:25:13',NULL),(12,156,'编写接口示例',0,2,'2026-05-07 22:25:13',NULL),(13,157,'确定数据表结构',0,0,'2026-05-07 22:25:13',NULL),(14,157,'设计字段类型和长度',0,1,'2026-05-07 22:25:13',NULL),(15,157,'设置索引和约束',0,2,'2026-05-07 22:25:13',NULL),(16,158,'前端开发',0,0,'2026-05-07 22:37:39',NULL),(17,158,'后端开发',0,1,'2026-05-07 22:37:39',NULL),(18,158,'接口文档编写',0,2,'2026-05-07 22:37:39',NULL),(19,158,'数据库表设计',0,3,'2026-05-07 22:37:39',NULL),(20,159,'前端开发',0,0,'2026-05-07 22:41:25',NULL),(21,159,'后端开发',0,1,'2026-05-07 22:41:25',NULL),(22,159,'接口文档编写',0,2,'2026-05-07 22:41:25',NULL),(23,159,'数据库表设计',0,3,'2026-05-07 22:41:25',NULL),(24,160,'前端开发',0,0,'2026-05-07 22:44:11',NULL),(25,160,'后端开发',0,1,'2026-05-07 22:44:11',NULL),(26,160,'接口文档编写',0,2,'2026-05-07 22:44:11',NULL),(27,160,'数据库表设计',0,3,'2026-05-07 22:44:11',NULL),(28,161,'前端开发',0,0,'2026-05-07 22:48:52',NULL),(29,161,'后端开发',0,1,'2026-05-07 22:48:52',NULL),(30,161,'接口文档编写',0,2,'2026-05-07 22:48:52',NULL),(31,161,'数据库表设计',0,3,'2026-05-07 22:48:52',NULL),(32,162,'前端开发',0,0,'2026-05-07 23:01:07',NULL),(33,162,'后端开发',0,1,'2026-05-07 23:01:07',NULL),(34,162,'接口文档编写',0,2,'2026-05-07 23:01:07',NULL),(35,162,'数据库表设计',0,3,'2026-05-07 23:01:07',NULL),(36,163,'前端开发',0,0,'2026-05-07 23:02:26',NULL),(37,163,'后端开发',0,1,'2026-05-07 23:02:26',NULL),(38,163,'接口文档编写',0,2,'2026-05-07 23:02:26',NULL),(39,163,'数据库表设计',0,3,'2026-05-07 23:02:26',NULL),(40,164,'前端开发',0,0,'2026-05-07 23:03:36',NULL),(41,164,'后端开发',0,1,'2026-05-07 23:03:36',NULL),(42,164,'接口文档编写',0,2,'2026-05-07 23:03:36',NULL),(43,164,'数据库表设计',0,3,'2026-05-07 23:03:36',NULL),(44,165,'前端',0,0,'2026-05-07 23:09:54',NULL),(45,165,'后端',0,1,'2026-05-07 23:09:54',NULL),(46,165,'接口文档',0,2,'2026-05-07 23:09:54',NULL),(47,165,'数据库表设计',0,3,'2026-05-07 23:09:54',NULL),(48,166,'前端',0,0,'2026-05-07 23:13:06',NULL),(49,166,'后端',0,1,'2026-05-07 23:13:06',NULL),(50,166,'接口文档',0,2,'2026-05-07 23:13:06',NULL),(51,166,'数据库表设计',0,3,'2026-05-07 23:13:06',NULL),(52,167,'前端',0,0,'2026-05-07 23:16:45',NULL),(53,167,'后端',0,1,'2026-05-07 23:16:45',NULL),(54,167,'接口文档',0,2,'2026-05-07 23:16:45',NULL),(55,167,'数据库表设计',0,3,'2026-05-07 23:16:45',NULL),(56,168,'前端',0,0,'2026-05-07 23:18:39',NULL),(57,168,'后端',0,1,'2026-05-07 23:18:39',NULL),(58,168,'接口文档',0,2,'2026-05-07 23:18:39',NULL),(59,168,'数据库表设计',0,3,'2026-05-07 23:18:39',NULL),(60,169,'前端',0,0,'2026-05-07 23:35:29',NULL),(61,169,'后端',0,1,'2026-05-07 23:35:29',NULL),(62,169,'接口文档',0,2,'2026-05-07 23:35:29',NULL),(63,169,'数据库表设计',0,3,'2026-05-07 23:35:29',NULL),(64,170,'前端',0,0,'2026-05-07 23:38:25',NULL),(65,170,'后端',0,1,'2026-05-07 23:38:25',NULL),(66,170,'接口文档',0,2,'2026-05-07 23:38:25',NULL),(67,170,'数据库表设计',0,3,'2026-05-07 23:38:25',NULL),(68,171,'做前端',0,0,'2026-05-07 23:43:25',NULL),(69,171,'做后端',0,1,'2026-05-07 23:43:26',NULL),(70,171,'编写接口文档',0,2,'2026-05-07 23:43:26',NULL),(71,171,'设计数据库表',0,3,'2026-05-07 23:43:26',NULL),(72,172,'前端',0,0,'2026-05-07 23:46:29',NULL),(73,172,'后端',0,1,'2026-05-07 23:46:29',NULL),(74,172,'接口文档',0,2,'2026-05-07 23:46:29',NULL),(75,172,'数据库表设计',0,3,'2026-05-07 23:46:29',NULL),(76,180,'收集相关资料',0,0,'2026-05-08 16:59:23',NULL),(77,180,'撰写报告草稿',0,1,'2026-05-08 16:59:23',NULL),(78,180,'校对报告内容',0,2,'2026-05-08 16:59:23',NULL),(79,180,'准备提交',0,3,'2026-05-08 16:59:23',NULL);
/*!40000 ALTER TABLE `subtask` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务标题',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '任务详细描述',
  `priority` tinyint NOT NULL DEFAULT '3' COMMENT '优先级: 1-最高, 2-高, 3-中, 4-低, 5-最低',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待办, 1-进行中, 2-完成, 3-已取消, 4-已存档',
  `deadline` datetime DEFAULT NULL COMMENT '截止时间',
  `remind_time` datetime DEFAULT NULL,
  `estimated_seconds` int DEFAULT NULL,
  `actual_seconds` int DEFAULT NULL,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '任务分类',
  `tags` json DEFAULT NULL COMMENT '标签数组，如["学习", "工作", "紧急"]',
  `reminder_config` json DEFAULT NULL COMMENT '提醒配置: {"enable": true, "advance_hours": [1, 24], "repeat": "none"}',
  `is_ai_generated` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为AI智能生成(1-是,0-否)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `completed_time` datetime DEFAULT NULL COMMENT '完成时间',
  `recurrence_rule` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recurrence_end_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`,`status`) COMMENT '按用户和状态查询',
  KEY `idx_due_date` (`deadline`) COMMENT '按截止时间查询',
  KEY `idx_user_priority` (`user_id`,`priority`) COMMENT '按用户和优先级查询',
  KEY `idx_created_at` (`created_time`) COMMENT '按创建时间查询',
  KEY `idx_user_parent` (`user_id`),
  KEY `idx_user_status_priority_deadline` (`user_id`,`status`,`priority`,`deadline`),
  CONSTRAINT `fk_task_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=325 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task`
--

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
INSERT INTO `task` VALUES (185,4,'去上班','',0,3,NULL,'2026-05-09 09:00:00',0,0,'工作',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-08 17:51:38','2026-05-08 18:01:28',NULL,NULL,NULL),(186,4,'去上班','',2,3,NULL,'2026-05-09 09:00:00',0,0,'工作',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-08 18:01:37','2026-05-08 18:14:19',NULL,NULL,NULL),(187,4,'去上班','',1,3,NULL,'2026-05-09 09:00:00',0,0,'工作',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-08 18:15:07','2026-05-08 19:03:01',NULL,NULL,NULL),(188,4,'去上班','明天上午9点我要去上班',1,3,NULL,'2026-05-09 09:00:00',3600,0,'工作',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-08 18:35:36','2026-05-08 19:03:10',NULL,NULL,NULL),(189,4,'完成毕设','毕设10天后截止，我现在不想做，但是我三天内应该可以做完',1,3,'2026-05-18 00:00:00',NULL,259200,0,'学习',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-08 18:36:50','2026-05-08 19:03:08',NULL,NULL,NULL),(190,4,'完成毕设','毕设项目，需在10天后截止，计划三天内完成',0,3,'2026-05-18 23:59:59','2026-05-11 15:00:00',259200,0,'学业毕设',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-08 18:54:41','2026-05-08 19:03:04',NULL,NULL,NULL),(201,4,'去上班','',1,3,NULL,'2026-05-09 09:00:00',0,0,'工作',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-08 19:26:41','2026-05-08 21:16:11',NULL,NULL,NULL),(203,4,'撤退','',2,3,NULL,'2026-05-09 22:00:00',0,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": [1]}',1,'2026-05-08 22:58:37','2026-05-08 23:02:25',NULL,'DAILY',NULL),(204,4,'撤退','',2,3,NULL,'2026-05-09 22:00:00',0,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": [1]}',1,'2026-05-08 22:59:37','2026-05-08 23:02:22',NULL,'DAILY',NULL),(205,4,'撤退','',2,3,NULL,'2026-05-09 22:00:00',0,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": [1]}',1,'2026-05-08 23:00:18','2026-05-08 23:02:20',NULL,'DAILY',NULL),(207,4,'洗漱睡觉','',2,3,NULL,'2026-05-08 23:02:00',120,0,'生活',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-08 23:59:24','2026-05-09 00:01:49',NULL,NULL,NULL),(208,4,'洗漱睡觉','',2,3,NULL,'2026-05-09 00:02:00',120,0,'日常作息',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-09 00:02:02','2026-05-09 00:02:44',NULL,NULL,NULL),(209,4,'洗漱睡觉','',2,3,NULL,'2026-05-09 00:04:25',0,0,'日常生活',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-09 00:02:34','2026-05-09 00:02:46',NULL,NULL,NULL),(210,4,'洗漱睡觉','',2,3,NULL,'2026-05-09 00:08:44',0,0,'生活',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-09 00:06:52','2026-05-09 00:11:18',NULL,NULL,NULL),(211,4,'洗漱睡觉','',2,3,NULL,'2026-05-09 00:13:24',7200,0,'生活',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-09 00:11:32','2026-05-09 00:14:52',NULL,NULL,NULL),(212,4,'洗漱睡觉','两分钟后开始洗漱睡觉',2,3,NULL,'2026-05-09 00:16:58',7200,0,'日常生活',NULL,'{\"enable\": true, \"repeat\": \"none\", \"advance_hours\": [1]}',1,'2026-05-09 00:15:05','2026-05-09 00:39:39',NULL,NULL,NULL),(213,4,'洗漱睡觉','两分钟后进行洗漱和睡觉',2,3,NULL,'2026-05-09 00:36:30',86400,0,'生活',NULL,'{\"enable\": false}',1,'2026-05-09 00:34:39','2026-05-09 00:39:42',NULL,NULL,NULL),(214,4,'洗漱睡觉','两分钟后开始洗漱并准备睡觉',2,3,NULL,'2026-05-09 00:53:56',86400,0,'日常生活',NULL,'{\"enable\": false}',1,'2026-05-09 00:52:06','2026-05-09 01:02:40',NULL,NULL,NULL),(215,4,'洗漱睡觉','两分钟后进行洗漱和睡觉',2,3,NULL,'2026-05-09 01:04:43',7200,0,'日常生活',NULL,'{\"enable\": false}',1,'2026-05-09 01:02:49','2026-05-09 01:10:35',NULL,NULL,NULL),(216,4,'睡觉','',2,3,NULL,'2026-05-09 01:12:00',0,0,'生活',NULL,'{\"enable\": false}',1,'2026-05-09 01:10:53','2026-05-09 01:16:17',NULL,NULL,NULL),(217,4,'睡觉','',2,3,NULL,'2026-05-09 01:21:24',0,0,'生活',NULL,'{\"enable\": false}',1,'2026-05-09 01:19:29','2026-05-09 01:33:42',NULL,NULL,NULL),(218,4,'睡觉','',2,3,NULL,'2026-05-09 01:35:54',0,0,'生活',NULL,'{\"enable\": false}',1,'2026-05-09 01:34:01','2026-05-09 01:44:22',NULL,NULL,NULL),(219,4,'睡觉','',2,3,NULL,'2026-05-09 01:46:29',0,0,'生活',NULL,'{\"enable\": false}',1,'2026-05-09 01:44:34','2026-05-09 01:53:37',NULL,NULL,NULL),(220,4,'睡觉','',2,3,NULL,'2026-05-09 01:55:45',0,0,'生活',NULL,'{\"enable\": false}',1,'2026-05-09 01:53:51','2026-05-09 02:05:42',NULL,NULL,NULL),(221,4,'睡觉','',2,3,NULL,'2026-05-09 02:07:50',0,0,'生活',NULL,'{\"enable\": false}',1,'2026-05-09 02:05:56','2026-05-09 02:13:17',NULL,NULL,NULL),(222,4,'睡觉','',2,3,NULL,'2026-05-09 02:15:30',0,0,'生活',NULL,'{\"enable\": false}',1,'2026-05-09 02:13:36','2026-05-09 12:34:11',NULL,NULL,NULL),(227,4,'睡觉','',2,3,NULL,'2026-05-09 12:44:46',0,0,'生活',NULL,'{\"enable\": false}',1,'2026-05-09 12:42:54','2026-05-09 12:45:36',NULL,NULL,NULL),(228,4,'去上班','',1,3,NULL,'2026-05-10 09:00:00',0,0,'工作',NULL,'{\"enable\": false}',1,'2026-05-09 12:54:39','2026-05-09 12:55:13',NULL,NULL,NULL),(233,5,'每周二9点医院复诊','',1,3,NULL,'2026-05-11 09:00:00',0,0,'健康',NULL,'{\"enable\": false}',1,'2026-05-09 21:29:28','2026-05-09 21:37:20',NULL,'WEEKLY:2',NULL),(234,5,'每周二9点医院复诊','',1,3,NULL,'2026-05-11 09:00:00',0,0,'健康',NULL,'{\"enable\": false}',1,'2026-05-09 21:37:54','2026-05-09 21:42:35',NULL,'WEEKLY:1:2',NULL),(235,5,'医院复诊','每周二9点去医院复诊',1,3,NULL,'2026-05-11 09:00:00',3600,0,'健康',NULL,'{\"enable\": false}',1,'2026-05-09 21:42:57','2026-05-09 21:56:57',NULL,'WEEKLY:1:2',NULL),(236,5,'每周二9点医院复诊','',1,3,NULL,'2026-05-18 09:00:00',0,0,'健康',NULL,'{\"enable\": false}',1,'2026-05-09 21:57:15','2026-05-09 21:59:46',NULL,'WEEKLY:1:2',NULL),(237,5,'每周二9点医院复诊','',1,3,NULL,'2026-05-11 09:00:00',0,0,'健康',NULL,'{\"enable\": false}',1,'2026-05-09 21:59:47','2026-05-09 22:00:49',NULL,'WEEKLY:1:2',NULL),(246,4,'测试关联专注','测试专注记录保存功能',1,3,NULL,NULL,10,0,'测试','[\"测试\"]','{\"enable\": false, \"advance_minutes\": []}',0,'2026-05-10 17:58:56','2026-05-10 18:03:46',NULL,NULL,NULL),(247,4,'测试保存专注记录',NULL,0,3,NULL,NULL,10,0,'测试','[\"测试\"]','{\"enable\": false, \"advance_minutes\": []}',0,'2026-05-10 18:04:11','2026-05-10 18:07:11',NULL,NULL,NULL),(248,4,'测试关联任务',NULL,0,2,NULL,NULL,10,12,'测试','[\"测试\"]','{\"enable\": false, \"advance_minutes\": []}',0,'2026-05-10 18:07:35','2026-05-10 18:54:32','2026-05-10 18:54:32',NULL,NULL),(250,4,'meeting at 9am tomorrow','',1,3,NULL,'2026-05-11 09:00:00',0,0,'工作',NULL,'{\"enable\": false}',1,'2026-05-10 19:26:15','2026-05-10 19:27:26',NULL,NULL,NULL),(251,4,'去上班','',1,3,NULL,'2026-05-11 09:00:00',3600,0,'工作',NULL,'{\"enable\": false}',1,'2026-05-10 19:27:12','2026-05-10 19:27:42',NULL,NULL,NULL),(252,4,'每周二复诊','',1,3,NULL,'2026-05-17 09:00:00',0,0,'健康',NULL,'{\"enable\": false}',1,'2026-05-10 19:28:01','2026-05-10 19:45:10',NULL,'WEEKLY:1:2',NULL),(253,4,'每周二复诊','',1,3,NULL,'2026-05-12 09:00:00',0,0,'健康',NULL,'{\"enable\": false}',1,'2026-05-10 19:45:26','2026-05-10 19:46:00',NULL,'WEEKLY:1:2',NULL),(256,4,'手表充电','',2,0,NULL,'2026-05-17 21:00:00',3600,0,'生活',NULL,'{\"enable\": false}',1,'2026-05-10 21:00:10','2026-05-10 21:00:10',NULL,'WEEKLY:1:7',NULL),(265,4,'去上班','',1,3,NULL,'2026-05-12 09:00:00',0,0,'工作',NULL,'{\"enable\": false}',1,'2026-05-11 18:07:16','2026-05-11 18:07:26',NULL,NULL,NULL),(270,5,'起床','',2,3,NULL,'2026-05-12 07:30:00',0,0,'日常作息',NULL,'{\"enable\": false}',1,'2026-05-11 23:22:06','2026-05-11 23:33:41',NULL,'DAILY',NULL),(271,5,'数学备考+吃药','',2,3,NULL,'2026-05-12 08:00:00',0,0,'学习',NULL,'{\"enable\": false}',1,'2026-05-11 23:22:06','2026-05-11 23:33:41',NULL,'DAILY',NULL),(272,5,'吃饭','',2,3,NULL,'2026-05-12 11:00:00',0,0,'日常作息',NULL,'{\"enable\": false}',1,'2026-05-11 23:22:06','2026-05-11 23:33:41',NULL,'DAILY',NULL),(273,5,'睡觉','',2,3,NULL,'2026-05-12 12:30:00',0,0,'日常作息',NULL,'{\"enable\": false}',1,'2026-05-11 23:22:06','2026-05-11 23:33:41',NULL,'DAILY',NULL),(274,5,'起床+学习408','',2,3,NULL,'2026-05-12 13:00:00',0,0,'学习',NULL,'{\"enable\": false}',1,'2026-05-11 23:22:07','2026-05-11 23:33:41',NULL,'DAILY',NULL),(275,5,'吃饭','',2,3,NULL,'2026-05-12 17:15:00',0,0,'日常作息',NULL,'{\"enable\": false}',1,'2026-05-11 23:22:07','2026-05-11 23:33:42',NULL,'DAILY',NULL),(276,5,'查缺补漏','',2,3,NULL,'2026-05-12 18:15:00',0,0,'学习',NULL,'{\"enable\": false}',1,'2026-05-11 23:22:07','2026-05-11 23:33:42',NULL,'DAILY',NULL),(277,5,'搓项目+吃药','',2,3,NULL,'2026-05-12 20:30:00',0,0,'工作',NULL,'{\"enable\": false}',1,'2026-05-11 23:22:07','2026-05-11 23:33:42',NULL,'DAILY',NULL),(278,5,'撤退','',2,3,NULL,'2026-05-12 22:00:00',0,0,'日常作息',NULL,'{\"enable\": false}',1,'2026-05-11 23:22:07','2026-05-11 23:33:42',NULL,'DAILY',NULL),(279,5,'洗漱','',2,3,NULL,'2026-05-12 23:00:00',0,0,'日常作息',NULL,'{\"enable\": false}',1,'2026-05-11 23:22:07','2026-05-11 23:33:46',NULL,'DAILY',NULL),(280,5,'起床','',2,3,NULL,'2026-05-12 07:30:00',0,0,'日常作息',NULL,'{\"enable\": false}',1,'2026-05-11 23:28:26','2026-05-11 23:33:38',NULL,'DAILY',NULL),(281,5,'数学备考+吃药','',2,3,NULL,'2026-05-12 07:30:00',0,0,'学习',NULL,'{\"enable\": false}',1,'2026-05-11 23:28:26','2026-05-11 23:33:41',NULL,'DAILY',NULL),(282,5,'起床','',0,3,NULL,'2026-05-12 07:30:00',3600,0,'日常作息',NULL,'{\"enable\": false}',1,'2026-05-11 23:33:45','2026-05-11 23:34:15',NULL,'DAILY',NULL),(283,5,'数学备考+吃药','',0,3,NULL,'2026-05-12 07:30:00',3600,0,'学习',NULL,'{\"enable\": false}',1,'2026-05-11 23:33:46','2026-05-11 23:34:12',NULL,'DAILY',NULL),(290,4,'查缺补漏','',2,2,NULL,'2026-05-13 18:15:00',5400,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": []}',1,'2026-05-12 18:15:03','2026-05-13 17:26:14','2026-05-13 17:26:14','DAILY',NULL),(291,4,'搓项目','',2,3,NULL,'2026-05-13 20:30:00',10800,0,'日常作息',NULL,'{\"enable\": true, \"advance_minutes\": []}',1,'2026-05-12 20:30:04','2026-05-12 23:26:41',NULL,'DAILY',NULL),(304,4,'去上班','',0,3,NULL,'2026-05-14 09:00:00',0,0,'工作',NULL,'{\"enable\": false}',1,'2026-05-13 20:52:51','2026-05-13 20:53:03',NULL,NULL,NULL),(307,4,'去上班','',0,3,NULL,'2026-05-14 09:00:00',0,0,'工作',NULL,'{\"enable\": false}',1,'2026-05-13 23:38:27','2026-05-13 23:38:39',NULL,NULL,NULL),(308,4,'起床','',2,0,NULL,'2026-05-15 07:30:00',0,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": []}',1,'2026-05-14 11:24:32','2026-05-14 11:24:32',NULL,'DAILY',NULL),(309,4,'数学备考','',2,0,NULL,'2026-05-15 08:00:00',0,0,'日常作息',NULL,'{\"enable\": true, \"advance_minutes\": []}',1,'2026-05-14 11:24:32','2026-05-14 11:24:32',NULL,'DAILY',NULL),(310,4,'吃饭','',2,0,NULL,'2026-05-15 11:00:00',0,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": []}',1,'2026-05-14 11:24:32','2026-05-14 11:24:32',NULL,'DAILY',NULL),(311,4,'睡觉','',2,0,NULL,'2026-05-15 12:30:00',0,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": []}',1,'2026-05-14 12:30:06','2026-05-14 12:30:06',NULL,'DAILY',NULL),(312,4,'起床+学习408','',2,0,NULL,'2026-05-15 13:00:00',28800,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": []}',1,'2026-05-14 13:00:06','2026-05-14 13:00:06',NULL,'DAILY',NULL),(313,4,'吃饭','',2,0,NULL,'2026-05-15 17:15:00',5400,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": []}',1,'2026-05-14 17:15:06','2026-05-14 17:15:06',NULL,'DAILY',NULL),(314,4,'查缺补漏','',2,0,NULL,'2026-05-15 18:15:00',5400,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": []}',1,'2026-05-14 18:15:11','2026-05-14 18:15:11',NULL,'DAILY',NULL),(315,4,'测试专注饼图绘制',NULL,1,2,NULL,NULL,10,10,'测试','[\"测试\"]','{\"enable\": false, \"advance_minutes\": []}',0,'2026-05-14 18:45:41','2026-05-14 18:45:56','2026-05-14 18:45:56',NULL,NULL),(316,4,'新建任务覆盖已有任务的问题',NULL,1,2,NULL,NULL,60,60,'测试','null','{\"enable\": false, \"advance_minutes\": []}',0,'2026-05-14 19:18:25','2026-05-14 19:53:11','2026-05-14 19:53:11',NULL,NULL),(317,4,'新建任务覆盖问题',NULL,1,3,NULL,NULL,60,0,'测试','null','{\"enable\": false, \"advance_minutes\": []}',0,'2026-05-14 19:24:29','2026-05-14 19:25:09',NULL,NULL,NULL),(318,4,'',NULL,1,3,NULL,NULL,0,0,'学习','null','{\"enable\": false, \"advance_minutes\": []}',0,'2026-05-14 19:24:47','2026-05-14 19:24:52',NULL,NULL,NULL),(319,4,'撤退','',2,0,NULL,'2026-05-15 22:00:00',0,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": []}',1,'2026-05-14 22:00:07','2026-05-14 22:00:07',NULL,'DAILY',NULL),(320,4,'洗漱','',2,0,NULL,'2026-05-15 23:00:00',0,0,'日常作息',NULL,'{\"enable\": true, \"advance_hours\": []}',1,'2026-05-14 23:00:03','2026-05-14 23:00:03',NULL,'DAILY',NULL),(321,4,'睡觉','',0,3,NULL,'2026-05-15 02:29:54',0,0,'日常作息',NULL,'{\"enable\": false}',1,'2026-05-15 02:28:05','2026-05-15 02:39:31',NULL,NULL,NULL),(322,4,'',NULL,1,3,NULL,'2026-05-15 02:30:00',0,0,'测试',NULL,'{\"enable\": false, \"advance_minutes\": []}',0,'2026-05-15 02:29:13','2026-05-15 02:39:34',NULL,NULL,NULL),(323,4,'',NULL,1,3,NULL,'2026-05-15 02:40:00',0,0,'',NULL,'{\"enable\": false, \"advance_minutes\": []}',0,'2026-05-15 02:40:04','2026-05-15 02:40:17',NULL,NULL,NULL),(324,4,'',NULL,1,3,NULL,'2026-05-15 02:42:00',0,0,'',NULL,'{\"enable\": false, \"advance_minutes\": []}',0,'2026-05-15 02:40:51','2026-05-15 03:03:51',NULL,NULL,NULL);
/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名，唯一',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱，唯一',
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密后的密码',
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '头像URL',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '昵称',
  `settings` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '账户是否正常(1-是,0-否)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '账号状态：0=正常 1=永久禁用 2=临时禁用',
  `ban_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '禁用原因（用户可见）',
  `ban_end_time` datetime DEFAULT NULL COMMENT '临时禁用到期时间（永久禁用为NULL）',
  `ban_operator` bigint DEFAULT NULL COMMENT '操作管理员ID',
  `ban_time` datetime DEFAULT NULL COMMENT '禁用时间',
  `role` tinyint(1) NOT NULL DEFAULT '0' COMMENT '用户角色：0=普通用户，1=管理员',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `occupation` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '???',
  `birthday` date DEFAULT NULL COMMENT '???',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '??????',
  `gender` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hobbies` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bio` text COLLATE utf8mb4_unicode_ci,
  `we_chat_webhook_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '企业微信机器人webhook地址',
  `ai_api_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `daily_focus_goal` int DEFAULT '7200' COMMENT '每日专注目标(秒)',
  `min_effective_duration` int DEFAULT '1800' COMMENT '最低有效专注时长(秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_created_at` (`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (4,'未来注定之人','2821811489@qq.com','$2a$10$tC6NYUTXy8/ca.F6Zha6eOkt95YlIQYsmntbMKvGL/Mxk.oZw5TMW','/api/avatar/4_20260430133346_6912bb9f.jpg','未来注定之人','408大神',1,0,NULL,NULL,NULL,NULL,1,'2026-05-15 03:03:45','2026-04-29 21:18:11','2026-05-15 02:25:09','学生','2005-04-24','19883139676','男','浙江杭州','唱歌','test','tlGoDHpztseq/tYKJ9P+SFaxclKent8Yz5JiqMzPiTj87+Cal/aZmK9tPUZolJW+sGrYTt3yo7DBmFYd1SNtiOcJB7brSpFtamoY4nnhJm61p9Hw4iYrgEK9JPbD6qJEXQjZTnSUhlkFD5zmZuRooHOmGHgD','/abFOfrqlo91J+h9ZzM4lptw1l9gO61+9SX7vWd3s4EhL/hMvqQgFSouZqPI9BfMlh7hIjqzDGu5AN/LfWKLcefAf0oFlaZYKetZzl8=',7200,1800),(5,'230110900804','19883139676@163.com','$2a$10$8tS4jAB/GYnQneiicn2n.uT5eWJEfrBuzC4.Juzr5bAePuJUF0B6W','','','',1,0,'测试封禁',NULL,4,'2026-05-14 17:29:00',0,'2026-05-14 19:55:19','2026-04-30 14:56:14','2026-05-12 19:24:38','',NULL,'','','','','','','rfHSGd5A8T9fWeyL6etF9t9uzMKtp9AGtH/tIt7wcNl1jk7dd0cBFC6lZf9X1TBYP6zv+HFxBSHcmYc7MXtAhEDwVhbDphA2Iv34Nwk=',7200,1800),(8,'testuser',NULL,'$2a$10$/zdPEQALfPez9yFXJYUGdu70ykiB//gwZkd2VxxpgroZBQ0cZmoJ2','','',NULL,1,0,NULL,NULL,NULL,NULL,0,'2026-05-10 18:35:31','2026-05-10 18:30:34','2026-05-10 18:30:34',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,7200,1800);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_appeal`
--

DROP TABLE IF EXISTS `user_appeal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_appeal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '申诉用户ID',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户填写的申诉理由',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '申诉状态：0=待审核 1=通过（解封） 2=驳回',
  `audit_admin_id` bigint DEFAULT NULL COMMENT '审核管理员ID',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '管理员审核备注（用户可见，驳回必填）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户申诉表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_appeal`
--

LOCK TABLES `user_appeal` WRITE;
/*!40000 ALTER TABLE `user_appeal` DISABLE KEYS */;
INSERT INTO `user_appeal` VALUES (1,5,'莫名其妙就被封了，测试回复','2026-05-14 17:29:50','2026-05-14 17:29:50',1,4,'2026-05-14 17:30:11',NULL);
/*!40000 ALTER TABLE `user_appeal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_message`
--

DROP TABLE IF EXISTS `user_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `recipient_id` bigint NOT NULL COMMENT '接收者ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `message_type` tinyint NOT NULL DEFAULT '1' COMMENT '消息类型：1-系统通知(群发) 2-管理员私信',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读 1-已读',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_recipient_read` (`recipient_id`,`is_read`),
  KEY `idx_recipient_created` (`recipient_id`,`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_message`
--

LOCK TABLES `user_message` WRITE;
/*!40000 ALTER TABLE `user_message` DISABLE KEYS */;
INSERT INTO `user_message` VALUES (1,4,5,'欢迎您使用本网站','欢迎您使用本网站',2,1,'2026-05-14 12:53:51','2026-05-14 12:55:28'),(2,4,5,'公告','感谢您使用本网站',1,1,'2026-05-14 12:57:53','2026-05-14 12:59:31'),(3,4,8,'公告','感谢您使用本网站',1,0,'2026-05-14 12:57:53','2026-05-14 12:57:53'),(4,0,5,'账号已解封','管理员已解封您的账号，您现在可以正常登录使用。',2,1,'2026-05-14 16:54:50','2026-05-14 16:55:18'),(5,0,5,'账号申诉通过','您的账号申诉已通过，已恢复正常使用。',2,1,'2026-05-14 17:30:11','2026-05-14 17:30:54');
/*!40000 ALTER TABLE `user_message` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-15  3:51:19
