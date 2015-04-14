/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS.
 *
 * Copyright (C) 2015 Juraj Somorovsky
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
package de.rub.nds.tlsattacker.transport;

/**
 * @author Juraj Somorovsky <juraj.somorovsky@rub.de>
 */
public class TransportHandlerFactory {

    private TransportHandlerFactory() {

    }

    public static TransportHandler createTransportHandler() {
	return new SimpleTransportHandler();
    }

    public static TransportHandler createTransportHandler(TransportHandlerType type, int maxResponseDelay) {
	switch (type) {
	    case SIMPLE:
		SimpleTransportHandler th = new SimpleTransportHandler();
		th.setMaxResponseWait(maxResponseDelay);
		return th;
	    case EAP_TLS:
		return new EAPTLSTransportHandler();
	    default:
		throw new UnsupportedOperationException("This transport handler " + "type is not supported");
	}
    }
}