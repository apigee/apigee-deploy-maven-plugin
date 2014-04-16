import os
offlineFilepath  = "/tmp/OFFLINE"
offlineFileExists = os.path.exists(offlineFilepath)
if(offlineFileExists):
    messageContext.variables["offlineFileExists"] = "true"
else:
    messageContext.variables["offlineFileExists"] = "false"
#----------------------------[ messageContext ]-----------------------

#variableName = "_transportvar.Authorization" # any variable in scope or predefined variable
#try:
#    variableValue = messageContext.variables[variableName]
#except PyException:
#    variableValue = "variable with specified name does not exit"

#xpathName = "customerId" # any messageXPath variable in scope
#try:
# 	xpathValue = messageContext.xpaths[xpathName].getResultAsString()
#except PyException:
#   xpathValue = "xpath with specified name does not exit"

#----------------------------[ Cryptography ]-----------------------
#from com.sonoa.script.runtime import Crypto

#input = "sample input string"
#base64encodedInput = Crypto.base64encode(input)
#inputDigest = Crypto.digest(Crypto.DIGEST_SHA1, input)

#scheme = Crypto.SCHEME_DES_ECB_PKCS5PADDING  # can be Crypto.SCHEME_DES_ECB_PKCS5PADDING or 
                                               #        Crypto.SCHEME_DESEDE_ECB_PKCS5PADDING or 
                                               #        Crypto.SCHEME_AES_ECB_PKCS5PADDING
#key = "secret key"
#encryptedInput = Crypto.encrypt(scheme, input, key)
#decryptedInput = Crypto.encrypt(scheme, encryptedInput, key)

#scheme = Crypto.SCHEME_PBE_WITH_MD5_AND_DES
#salt = "salt1234" # must be 8 characters
#encryptedInput = Crypto.encrypt(scheme, input, key, salt)
#decryptedInput = Crypto.encrypt(scheme, encryptedInput, key, salt)