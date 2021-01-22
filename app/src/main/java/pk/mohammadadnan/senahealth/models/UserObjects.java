package pk.mohammadadnan.senahealth.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserObjects {

    @SerializedName("response")
    public Response response;

    public UserObjects(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public class Response{
        @SerializedName("results")
        public List<Results> results;

        public Response(List<Results> results) {
            this.results = results;
        }

        public List<Results> getResults() {
            return results;
        }

        public class Results{
            @SerializedName("avatar")
            public String avatar;
            @SerializedName("firstName")
            public String firstName;
            @SerializedName("lastName")
            public String lastName;
            @SerializedName("sha256")
            public String sha256;
            @SerializedName("_id")
            public String _id;
            @SerializedName("authentication")
            public Authentication authentication;

            public Results(String avatar, String firstName, String lastName, String sha256, String _id, Authentication authentication) {
                this.avatar = avatar;
                this.firstName = firstName;
                this.lastName = lastName;
                this.sha256 = sha256;
                this._id = _id;
                this.authentication = authentication;
            }

            public Results(String avatar, String firstName, String lastName, String sha256) {
                this.avatar = avatar;
                this.firstName = firstName;
                this.lastName = lastName;
                this.sha256 = sha256;
            }

            public String getAvatar() {
                return avatar;
            }

            public String getFirstName() {
                return firstName;
            }

            public String getLastName() {
                return lastName;
            }

            public String getSha256() {
                return sha256;
            }

            public String get_id() {
                return _id;
            }

            public Authentication getAuthentication() {
                return authentication;
            }

            public class Authentication{
                @SerializedName("email")
                public Email email;

                public Authentication(Email email) {
                    this.email = email;
                }

                public Email getEmailObject() {
                    return email;
                }

                public class Email{
                    @SerializedName("email")
                    public String email;

                    public Email(String email) {
                        this.email = email;
                    }

                    public String getEmailAddress() {
                        return email;
                    }
                }
            }
        }
    }

}
