const toggleSidebar = () => {
  if ($(".sidebar").is(":visible")) {
    $(".sidebar").css("display", "none");
    $(".content").css("margin-left", "1%");
    //      $('.content').css("padding-left", "18%");
  } else {
    $(".sidebar").css("display", "block");
    $(".content").css("margin-left", "18%");
    //		$('.content').css("padding-left", "1%");
  }
};

//first request to create order

const paymentstart = () => {
  var payment = $("#payment").val();
  console.log("payment=" + payment);
  if (payment == "" || payment == null) {
    alert("Amount is required !!!");
    return;
  }

  $.ajax({
    url: "/user/create_order",
    data: JSON.stringify({ payment: payment, info: "order_request" }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (response) {
      console.log(response);
      if (response.status == "created") {
        let option = {
          key: "rzp_test_OjUYiPMUkioMby",
          amount: response.amount,
          currency: "INR",
          name: "Smart Contact Manager",
          description: "Donation",
          image:
            "https://media.licdn.com/dms/image/D5635AQH_NzViK6ffeg/profile-framedphoto-shrink_200_200/0/1669804076571?e=1679821200&v=beta&t=RsgA4oIAuvHR1hLUOLW-Wyjk_arhe3VXTuXCHwCRgI8",
          order_id: response.id,
          handler: function (response) {
            alert(response.razorpay_payment_id);
            alert(response.razorpay_order_id);
            alert(response.razorpay_signature);
            console.log("Congrats ,Payment Successfully");
            alert("Congrats ,Payment Successfully");
          },
          prefill: {
            name: "bhagyashri deokar",
            email: "deokar782@gmail.com",
            contact: "9309137117",
          },
          notes: {
            address: "Contact Management System-B",
          },
          theme: {
            color: "#FFFFFF",
          },
        };
        var rzp1 = new Razorpay(option);
        rzp1.on("payment.failed", function (response) {
          console.log(response.error.code);
          console.log(response.error.description);
          console.log(response.error.source);
          console.log(response.error.step);
          console.log(response.error.reason);
          console.log(response.error.metadata.order_id);
          console.log(response.error.metadata.payment_id);
        });
        rzp1.open();
      }
    },
    error: function (error) {
      console.log(error);
      alert("something went wrong");
    },
  });
};
