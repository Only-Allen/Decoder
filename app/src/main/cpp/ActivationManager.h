#ifndef HSM_ACTIVATION_MANAGER_H
#define HSM_ACTIVATION_MANAGER_H

/**
 *  Return Codes
 */
#define LM_ERROR 							0	 ///< Unknown Error
#define LM_SUCCESS 							1	 ///< Successful activation
#define LM_ERROR_NOT_CREATED				2000 ///< No Manager exists, call one of the Create functions.
#define LM_ERROR_INSUFFICIENT_MEMORY		2001 ///< Malloc failed to return enough memory for Manager
#define LM_ERROR_ALREADY_CREATED			2002 ///< Create function was already called
#define LM_ERROR_FOLDER_PATH_ERROR			2003 ///< Folder path was not included or copy failed
#define LM_ERROR_DEVICEID_ERROR				2004 ///< Device ID was not included or copy failed
#define LM_ERROR_DEVICETYPE_ERROR			2005 ///< Device Type was not included or copy failed
#define LM_ERROR_CUSTOMERID_ERROR			2006 ///< Activation ID was not included or copy failed
#define LM_ERROR_DEACTIVATION_NOT_ALLOWED	2007 ///< Deactivation is not allowed for your license type
#define LM_ERROR_CURL_FAILURE				5000 ///< Curl returned an error while contacting the server.
#define LM_ERROR_FLEXERA_SERVER_FAILURE		3000 ///< Base for Flexera server communication status codes.
#define LM_ERROR_FLEXERA_COM_FAILURE		4000 ///< Forced communication but received no response from server.
#define LM_ERROR_FLEXERA_NO_RESPONSE		4001 ///< Communicated to the server but didn't receive a proper response.

/**
 * @brief Checks if the API has been activated, if so nothing more is required
 *
 * @param activaitonKey 
 * 	 The license key provided with your SDK
 * 
 * @param writePath 
 * 	 A writable path on your device where the API can store information (e.g. "/data/data/com.test")
 *
 * @return 
 * 	 returns 0 if not activated, 1 if activated
 */
int IsActivated(const char *activaitonKey, const char *writePath);

/**
 * @brief Activates the SwiftDecoder API against the Honeywell back-end server.  Once activation against the back-end server has occurred once, the device will no longer require external connectivity.  
 *		  However, the ActivateAPI method will still need to be called each time the application is first ran in order to use the API.                    
 * 
 * @param activaitonKey 
 * 	 The license key provided with your SDK
 * 
 * @param writePath 
 * 	 A writable path on your device where the API can store information (e.g. "/data/data/com.test")
 */
int ActivateAPI( const char *activaitonKey, const char *writePath );

/**
 * @brief Activates the SwiftDecoder API against a local license server instance.  Once activation against the local license server has occurred once, the device will no longer require connectivity to the license server.  
 *		  However, the ActivateAPIWithLocalServer method will still need to be called each time the application is first ran in order to use the API.                    
 * 
 * @param activaitonKey 
 * 	 The license key provided with your SDK
 * 
 * @param writePath 
 * 	 A writable path on your device where the API can store information (e.g. "/data/data/com.test")
 * 
 * @param serverURL 
 * 	 The URL of the local licensing server including port (e.g. "http://192.168.1.1:7070" )
 * 
 * @param identityClient 
 * 	 The identity client data for your local server instance.  This is the contents of the IdentityClient.bin file delivered with your licensing server
 *
 * @param identityClientLength 
 * 	 The length of the identity client data
 */
int ActivateAPIWithLocalServer( const char *activaitonKey, const char *writePath, const char *serverURL, unsigned char *identityClient, int identityClientLength );

/**
 * @brief Generates a license request payload that will need to be sent to the license server via HTTP POST request
* 
 * @param activaitonKey 
 * 	 The license key provided with your SDK
 * 
 * @param writePath 
 * 	 A writable path on your device where the API can store information (e.g. "/data/data/com.test")
 * 
 * @param identityClient 
 * 	 The identity client data for your local server instance.  This is the contents of the IdentityClient.bin file delivered with your licensing server
 *
 * @param identityClientLength 
 * 	 The length of the identity client data
 * 
 * @param featureRequest 
 * 	 The contents of the SDM-ALL.features file delivered with your SDK
 *
 * @param featureRequestLength 
 * 	 The length of the featureRequest file data
 *
 * \param liceseRequestBuffer 
 * 	 A buffer for the license request payload.  This is the payload that will need to be sent to the license server via HTTP POST request (Note: memory internally allocated, you are responsible for freeing!)
*
 * \return 
 * 	 The length of the license request payload
*/
int GenerateLicRequest( const char *activaitonKey, const char *writePath, unsigned char *identityClient, int identityClientLength, unsigned char *featureRequest, int featureRequestLength, unsigned char ** licenseRequestBuffer );

/**
 * @brief Consume a license response payload that was received from the license server HTTP POST request
* 
 * @param activaitonKey 
 * 	 The license key provided with your SDK
 * 
 * @param writePath 
 * 	 A writable path on your device where the API can store information (e.g. "/data/data/com.test")
 * 
 * @param identityClient 
 * 	 The identity client data for your local server instance.  This is the contents of the IdentityClient.bin file delivered with your licensing server
 *
 * @param identityClientLength 
 * 	 The length of the identity client data
 * 
 * @param licenseResponse 
 * 	 The license response payload that was received back from the license server HTTP POST request
 *
 * @param featureRequestLength 
 * 	 The length of the licenseResponse payload data
*
 * @return 
 * 	 The result of the activation
*/
int ConsumeLicResponse( const char *activaitonKey, const char *writePath, unsigned char *identityClient, int identityClientLength, unsigned char *licesneResponse, int licesneResponseLength );

//int test_chx();
#endif
