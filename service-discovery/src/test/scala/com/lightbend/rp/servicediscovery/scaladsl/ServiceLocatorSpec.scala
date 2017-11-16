/*
 * Copyright 2017 Lightbend, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.rp.servicediscovery.scaladsl

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.typesafe.config.ConfigFactory
import java.net.URI
import org.scalatest.{ AsyncWordSpecLike, BeforeAndAfterAll, Matchers }

object ServiceLocatorSpec {
  def config = ConfigFactory
    .parseString(
      s"""|reactive-lib.service-discovery {
          |  external-service-addresses {
          |    "has-one" = ["http://127.0.0.1:9000"]
          |    "has-two" = ["http://127.0.0.1:8000", "http://127.0.0.1:8001"]
          |  }
          |}
          |""".stripMargin)
    .withFallback(ConfigFactory.defaultApplication())
}

class ServiceLocatorSpec extends TestKit(ActorSystem("service-locator", ServiceLocatorSpec.config))
  with ImplicitSender
  with AsyncWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  implicit val settings = Settings(system)

  "ServiceLocator" should {
    "resolve external services correctly (one)" in {
      ServiceLocator
        .lookup("has-one")
        .map(_.contains(new URI("http://127.0.0.1:9000")) shouldBe true)
    }

    "resolve external services correctly (many)" in {
      ServiceLocator
        .lookup("has-two")
        .map(e =>
          e.contains(new URI("http://127.0.0.1:8000")) || e.contains(new URI("http://127.0.0.1:8001")) shouldBe true
        )
    }
  }
}