package main.service.user;

import main.api.request.EditProfileRequest;
import main.api.request.GetRestoreRequest;
import main.api.request.RegisterRequest;
import main.api.request.RestoreRequest;
import main.api.response.EditProfileResponse;
import main.api.response.RegisterResponse;
import main.api.response.RestoreResponse;

public interface UserService {

  RegisterResponse addUser(RegisterRequest request);

  EditProfileResponse editUser(EditProfileRequest request);

  RestoreResponse sendRestoreRequestUser(GetRestoreRequest request);

  RestoreResponse restoreUser(RestoreRequest request);
}