/*
 * Copyright (C) 2011-2012 spray.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spray.httpx

import marshalling.{ Marshaller, MetaMarshallers }
import unmarshalling.Unmarshaller
import spray.http.MediaTypes._
import spray.http.{ ContentType, HttpBody }
import org.json4s.jackson.Serialization.{ read, write }
import org.json4s.Formats

trait Json4sJacksonSupport extends MetaMarshallers {
  implicit def formats: Formats

  implicit def json4sUnmarshaller[T: Manifest] =
    Unmarshaller[T](`application/json`) {
      case x: HttpBody ⇒ read[T](x.asString)
    }

  implicit def json4sMarshaller[T <: AnyRef] =
    Marshaller.delegate[T, String](ContentType.`application/json`) {
      write(_)
    }
}
